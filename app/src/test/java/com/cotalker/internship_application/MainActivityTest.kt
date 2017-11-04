package com.cotalker.internship_application

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 * Created by sariego on 11/3/17.
 */
class MainActivityTest {

    lateinit var activity: MainActivity

    @Before
    fun setup() {
        activity = MainActivity()
    }

    @Test
    fun replaceAsterisks() {
        assertEquals(listOf("1001000", "1001010", "1011000", "1011010"), activity.replaceAsterisks("10*10*0"))
        assertEquals(listOf("00", "10"), activity.replaceAsterisks("*0"))
    }

}