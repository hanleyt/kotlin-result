package com.github.michaelbull.result

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.sameInstance
import org.junit.jupiter.api.Test

internal class IterableTest {
    private sealed class IterableError {
        object IterableError1 : IterableError()
        object IterableError2 : IterableError()
    }

    private fun sameError(error: IterableError): Matcher<IterableError> {
        return sameInstance(error)
    }

    @Test
    internal fun `fold should return the accumulated value if ok`() {
        val result = listOf(20, 30, 40, 50).fold(
            initial = 10,
            operation = { a, b -> Ok(a + b) }
        )

        result as Ok

        assertThat(result.value, equalTo(150))
    }

    @Test
    internal fun `fold should return the first error if not ok`() {
        val result: Result<Int, IterableError> = listOf(5, 10, 15, 20, 25).fold(
            initial = 1,
            operation = { a, b ->
                when (b) {
                    (5 + 10) -> Err(IterableError.IterableError1)
                    (5 + 10 + 15 + 20) -> Err(IterableError.IterableError2)
                    else -> Ok(a * b)
                }
            }
        )

        result as Err

        val matcher: Matcher<IterableError> = sameInstance(IterableError.IterableError1)
        assertThat(result.error, matcher)
    }

    @Test
    internal fun `foldRight should return the accumulated value if ok`() {
        val result = listOf(2, 5, 10, 20).foldRight(
            initial = 100,
            operation = { a, b -> Ok(b - a) }
        )

        result as Ok

        assertThat(result.value, equalTo(63))
    }

    @Test
    internal fun `foldRight should return the last error if not ok`() {
        val result = listOf(2, 5, 10, 20, 40).foldRight(
            initial = 38500,
            operation = { a, b ->
                when (b) {
                    (((38500 / 40) / 20) / 10) -> Err(IterableError.IterableError1)
                    ((38500 / 40) / 20) -> Err(IterableError.IterableError2)
                    else -> Ok(b / a)
                }
            }
        )

        result as Err

        assertThat(result.error, sameError(IterableError.IterableError2))
    }

    @Test
    internal fun `combine should return the combined list of values if results are ok`() {
        val values = combine(
            Ok(10),
            Ok(20),
            Ok(30)
        ).get()!!

        assertThat(values.size, equalTo(3))
        assertThat(values[0], equalTo(10))
        assertThat(values[1], equalTo(20))
        assertThat(values[2], equalTo(30))
    }

    @Test
    internal fun `combine should return the first error if results are not ok`() {
        val result = combine(
            Ok(20),
            Ok(40),
            Err(IterableError.IterableError1),
            Ok(60),
            Err(IterableError.IterableError2),
            Ok(80)
        )

        result as Err

        assertThat(result.error, sameError(IterableError.IterableError1))
    }

    @Test
    internal fun `getAll should return all of the result values`() {
        val values = getAll(
            Ok("hello"),
            Ok("big"),
            Err(IterableError.IterableError2),
            Ok("wide"),
            Err(IterableError.IterableError1),
            Ok("world")
        )

        assertThat(values.size, equalTo(4))
        assertThat(values[0], equalTo("hello"))
        assertThat(values[1], equalTo("big"))
        assertThat(values[2], equalTo("wide"))
        assertThat(values[3], equalTo("world"))
    }

    @Test
    internal fun `getAllErrors should return all of the result errors`() {
        val errors = getAllErrors(
            Err(IterableError.IterableError2),
            Ok("haskell"),
            Err(IterableError.IterableError2),
            Ok("f#"),
            Err(IterableError.IterableError1),
            Ok("elm"),
            Err(IterableError.IterableError1),
            Ok("clojure"),
            Err(IterableError.IterableError2)
        )

        assertThat(errors.size, equalTo(5))
        assertThat(errors[0], sameError(IterableError.IterableError2))
        assertThat(errors[1], sameError(IterableError.IterableError2))
        assertThat(errors[2], sameError(IterableError.IterableError1))
        assertThat(errors[3], sameError(IterableError.IterableError1))
        assertThat(errors[4], sameError(IterableError.IterableError2))
    }

    @Test
    internal fun `partition should return a pair of all the result values and errors`() {
        val pairs = partition(
            Err(IterableError.IterableError2),
            Ok("haskell"),
            Err(IterableError.IterableError2),
            Ok("f#"),
            Err(IterableError.IterableError1),
            Ok("elm"),
            Err(IterableError.IterableError1),
            Ok("clojure"),
            Err(IterableError.IterableError2)
        )

        val values = pairs.first
        assertThat(values.size, equalTo(4))
        assertThat(values[0], equalTo("haskell"))
        assertThat(values[1], equalTo("f#"))
        assertThat(values[2], equalTo("elm"))
        assertThat(values[3], equalTo("clojure"))

        val errors = pairs.second
        assertThat(errors.size, equalTo(5))
        assertThat(errors[0], sameError(IterableError.IterableError2))
        assertThat(errors[1], sameError(IterableError.IterableError2))
        assertThat(errors[2], sameError(IterableError.IterableError1))
        assertThat(errors[3], sameError(IterableError.IterableError1))
        assertThat(errors[4], sameError(IterableError.IterableError2))
    }
}
