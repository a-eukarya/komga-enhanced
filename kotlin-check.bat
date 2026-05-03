call gradlew.bat clean :komga:compileKotlin --parallel
call gradlew.bat clean :komga:ktlintMainSourceSetCheck --parallel
call gradlew.bat clean test --parallel
pause