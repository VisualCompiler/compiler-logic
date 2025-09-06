#!/usr/bin/env kotlin

import java.io.File

fun main() {
    val testFiles = listOf(
        "src/jsTest/kotlin/integration/ValidTestCases.kt",
        "src/jsTest/kotlin/parser/LabelAnalysisTest.kt", 
        "src/jsTest/kotlin/parser/VariableResolutionTest.kt"
    )
    
    testFiles.forEach { filePath ->
        val file = File(filePath)
        if (file.exists()) {
            var content = file.readText()
            println("Processing $filePath...")
            
            // Fix ExpressionStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("ExpressionStatement\\(location = [^,]+,\\s*"),
                "ExpressionStatement("
            )
            
            // Fix AssignmentExpression constructors - remove named parameters and fix order
            content = content.replace(
                Regex("AssignmentExpression\\(\\s*lvalue = "),
                "AssignmentExpression("
            )
            content = content.replace(
                Regex("AssignmentExpression\\(\\s*rvalue = "),
                "AssignmentExpression("
            )
            
            // Fix BinaryExpression constructors - remove named parameters and fix order
            content = content.replace(
                Regex("BinaryExpression\\(\\s*left = "),
                "BinaryExpression("
            )
            content = content.replace(
                Regex("BinaryExpression\\(\\s*operator = "),
                "BinaryExpression("
            )
            content = content.replace(
                Regex("BinaryExpression\\(\\s*right = "),
                "BinaryExpression("
            )
            
            // Fix UnaryExpression constructors - remove named parameters and fix order
            content = content.replace(
                Regex("UnaryExpression\\(\\s*operator = "),
                "UnaryExpression("
            )
            content = content.replace(
                Regex("UnaryExpression\\(\\s*expression = "),
                "UnaryExpression("
            )
            
            // Fix VariableExpression constructors - remove named parameters and fix order
            content = content.replace(
                Regex("VariableExpression\\(\\s*name = "),
                "VariableExpression("
            )
            
            // Fix IntExpression constructors - remove named parameters and fix order
            content = content.replace(
                Regex("IntExpression\\(\\s*value = "),
                "IntExpression("
            )
            
            // Fix ReturnStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("ReturnStatement\\(location = [^,]+,\\s*"),
                "ReturnStatement("
            )
            content = content.replace(
                Regex("ReturnStatement\\(\\s*expression = "),
                "ReturnStatement("
            )
            
            // Fix WhileStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("WhileStatement\\(\\s*condition = "),
                "WhileStatement("
            )
            content = content.replace(
                Regex("WhileStatement\\(\\s*body = "),
                "WhileStatement("
            )
            
            // Fix ForStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("ForStatement\\(\\s*init = "),
                "ForStatement("
            )
            content = content.replace(
                Regex("ForStatement\\(\\s*condition = "),
                "ForStatement("
            )
            content = content.replace(
                Regex("ForStatement\\(\\s*post = "),
                "ForStatement("
            )
            content = content.replace(
                Regex("ForStatement\\(\\s*body = "),
                "ForStatement("
            )
            
            // Fix DoWhileStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("DoWhileStatement\\(\\s*condition = "),
                "DoWhileStatement("
            )
            content = content.replace(
                Regex("DoWhileStatement\\(\\s*body = "),
                "DoWhileStatement("
            )
            
            // Fix IfStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("IfStatement\\(\\s*condition = "),
                "IfStatement("
            )
            content = content.replace(
                Regex("IfStatement\\(\\s*thenStatement = "),
                "IfStatement("
            )
            content = content.replace(
                Regex("IfStatement\\(\\s*elseStatement = "),
                "IfStatement("
            )
            
            // Fix LabeledStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("LabeledStatement\\(\\s*label = "),
                "LabeledStatement("
            )
            content = content.replace(
                Regex("LabeledStatement\\(\\s*statement = "),
                "LabeledStatement("
            )
            
            // Fix BreakStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("BreakStatement\\(\\s*label = "),
                "BreakStatement("
            )
            
            // Fix ContinueStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("ContinueStatement\\(\\s*label = "),
                "ContinueStatement("
            )
            
            // Fix GotoStatement constructors - remove named parameters and fix order
            content = content.replace(
                Regex("GotoStatement\\(\\s*label = "),
                "GotoStatement("
            )
            
            // Fix FunctionCall constructors - remove named parameters and fix order
            content = content.replace(
                Regex("FunctionCall\\(\\s*name = "),
                "FunctionCall("
            )
            content = content.replace(
                Regex("FunctionCall\\(\\s*arguments = "),
                "FunctionCall("
            )
            
            // Fix ConditionalExpression constructors - remove named parameters and fix order
            content = content.replace(
                Regex("ConditionalExpression\\(\\s*codition = "),
                "ConditionalExpression("
            )
            content = content.replace(
                Regex("ConditionalExpression\\(\\s*thenExpression = "),
                "ConditionalExpression("
            )
            content = content.replace(
                Regex("ConditionalExpression\\(\\s*elseExpression = "),
                "ConditionalExpression("
            )
            
            // Fix VariableDeclaration constructors - remove named parameters and fix order
            content = content.replace(
                Regex("VariableDeclaration\\(location = [^,]+,\\s*"),
                "VariableDeclaration("
            )
            content = content.replace(
                Regex("VariableDeclaration\\(\\s*name = "),
                "VariableDeclaration("
            )
            content = content.replace(
                Regex("VariableDeclaration\\(\\s*init = "),
                "VariableDeclaration("
            )
            
            // Fix FunctionDeclaration constructors - remove named parameters and fix order
            content = content.replace(
                Regex("FunctionDeclaration\\(location = [^,]+,\\s*"),
                "FunctionDeclaration("
            )
            content = content.replace(
                Regex("FunctionDeclaration\\(\\s*name = "),
                "FunctionDeclaration("
            )
            content = content.replace(
                Regex("FunctionDeclaration\\(\\s*parameters = "),
                "FunctionDeclaration("
            )
            content = content.replace(
                Regex("FunctionDeclaration\\(\\s*body = "),
                "FunctionDeclaration("
            )
            
            // Fix Block constructors - remove named parameters and fix order
            content = content.replace(
                Regex("Block\\(location = [^,]+,\\s*"),
                "Block("
            )
            content = content.replace(
                Regex("Block\\(\\s*items = "),
                "Block("
            )
            
            // Fix SimpleProgram constructors - remove named parameters and fix order
            content = content.replace(
                Regex("SimpleProgram\\(location = [^,]+,\\s*"),
                "SimpleProgram("
            )
            content = content.replace(
                Regex("SimpleProgram\\(\\s*functionDeclaration = "),
                "SimpleProgram("
            )
            
            // Fix S constructors - remove named parameters and fix order
            content = content.replace(
                Regex("S\\(\\s*statement = "),
                "S("
            )
            
            // Fix D constructors - remove named parameters and fix order
            content = content.replace(
                Regex("D\\(\\s*declaration = "),
                "D("
            )
            
            // Fix VarDecl constructors - remove named parameters and fix order
            content = content.replace(
                Regex("VarDecl\\(\\s*declaration = "),
                "VarDecl("
            )
            
            // Fix InitDeclaration constructors - remove named parameters and fix order
            content = content.replace(
                Regex("InitDeclaration\\(\\s*declaration = "),
                "InitDeclaration("
            )
            
            // Fix InitExpression constructors - remove named parameters and fix order
            content = content.replace(
                Regex("InitExpression\\(\\s*expression = "),
                "InitExpression("
            )
            
            // Now add location parameters to constructors that need them
            val constructorsNeedingLocation = listOf(
                "S", "D", "VarDecl", "FunDecl", "InitDeclaration", "InitExpression",
                "WhileStatement", "ForStatement", "DoWhileStatement", "IfStatement", 
                "LabeledStatement", "BreakStatement", "ContinueStatement", "GotoStatement",
                "FunctionCall", "ConditionalExpression", "VariableDeclaration", "FunctionDeclaration",
                "Block", "SimpleProgram", "ReturnStatement", "ExpressionStatement", "NullStatement"
            )
            
            constructorsNeedingLocation.forEach { constructor ->
                // Pattern: ConstructorName( but not ConstructorName(location = 
                val pattern = Regex("$constructor\\((?![^)]*location\\s*=)")
                content = content.replace(pattern) { matchResult ->
                    "$constructor(location = TEST_LOCATION, "
                }
            }
            
            // Fix specific issues with parameter order
            // Fix ExpressionStatement(expression, location) -> ExpressionStatement(expression, location)
            content = content.replace(
                Regex("ExpressionStatement\\(location = TEST_LOCATION,\\s*([^,]+),\\s*location = TEST_LOCATION\\)"),
                "ExpressionStatement($1, location = TEST_LOCATION)"
            )
            
            // Fix AssignmentExpression(lvalue, rvalue, location) -> AssignmentExpression(lvalue, rvalue, location)
            content = content.replace(
                Regex("AssignmentExpression\\(location = TEST_LOCATION,\\s*([^,]+),\\s*([^,]+),\\s*location = TEST_LOCATION\\)"),
                "AssignmentExpression($1, $2, location = TEST_LOCATION)"
            )
            
            // Fix BinaryExpression(left, operator, right, location) -> BinaryExpression(left, operator, right, location)
            content = content.replace(
                Regex("BinaryExpression\\(location = TEST_LOCATION,\\s*([^,]+),\\s*([^,]+),\\s*([^,]+),\\s*location = TEST_LOCATION\\)"),
                "BinaryExpression($1, $2, $3, location = TEST_LOCATION)"
            )
            
            // Fix UnaryExpression(operator, expression, location) -> UnaryExpression(operator, expression, location)
            content = content.replace(
                Regex("UnaryExpression\\(location = TEST_LOCATION,\\s*([^,]+),\\s*([^,]+),\\s*location = TEST_LOCATION\\)"),
                "UnaryExpression($1, $2, location = TEST_LOCATION)"
            )
            
            // Fix VariableExpression(name, location) -> VariableExpression(name, location)
            content = content.replace(
                Regex("VariableExpression\\(location = TEST_LOCATION,\\s*([^,]+),\\s*location = TEST_LOCATION\\)"),
                "VariableExpression($1, location = TEST_LOCATION)"
            )
            
            // Fix IntExpression(value, location) -> IntExpression(value, location)
            content = content.replace(
                Regex("IntExpression\\(location = TEST_LOCATION,\\s*([^,]+),\\s*location = TEST_LOCATION\\)"),
                "IntExpression($1, location = TEST_LOCATION)"
            )
            
            // Fix ReturnStatement(expression, location) -> ReturnStatement(expression, location)
            content = content.replace(
                Regex("ReturnStatement\\(location = TEST_LOCATION,\\s*([^,]+),\\s*location = TEST_LOCATION\\)"),
                "ReturnStatement($1, location = TEST_LOCATION)"
            )
            
            file.writeText(content)
            println("Fixed $filePath")
        }
    }
    
    println("Automated fix completed!")
}
