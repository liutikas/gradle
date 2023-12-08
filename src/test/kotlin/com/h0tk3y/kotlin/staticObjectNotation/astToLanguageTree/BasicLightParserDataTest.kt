package com.example.com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree

import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.AbstractBasicDataTest
import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.ElementResult
import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.ParseTestUtil

class BasicLightParserDataTest: AbstractBasicDataTest() {

    override fun parse(code: String): List<ElementResult<*>> = ParseTestUtil.parseWithLightParser(code)
}