#!/bin/bash

java Main ./testCases/BinaryTree.java
clang-4.0 -o "LLVMfiles/BinaryTree".out1 "LLVMfiles/BinaryTree".ll 
./"LLVMfiles/BinaryTree".out1
java Main ./testCases/BubbleSort.java
clang-4.0 -o "LLVMfiles/BubbleSort".out1 "LLVMfiles/BubbleSort".ll
./"LLVMfiles/BubbleSort".out1
java Main ./testCases/Factorial.java 
clang-4.0 -o "LLVMfiles/Factorial".out1 "LLVMfiles/Factorial".ll
./"LLVMfiles/Factorial".out1
java Main ./testCases/LinearSearch.java 
clang-4.0 -o "LLVMfiles/LinearSearch".out1 "LLVMfiles/LinearSearch".ll
./"LLVMfiles/LinearSearch".out1
java Main ./testCases/LinkedList.java 
clang-4.0 -o "LLVMfiles/LinkedList".out1 "LLVMfiles/LinkedList".ll
./"LLVMfiles/LinkedList".out1
java Main ./testCases/MoreThan4.java 
clang-4.0 -o "LLVMfiles/MoreThan4".out1 "LLVMfiles/MoreThan4".ll
./"LLVMfiles/MoreThan4".out1
java Main ./testCases/QuickSort.java 
clang-4.0 -o "LLVMfiles/QuickSort".out1 "LLVMfiles/QuickSort".ll
./"LLVMfiles/QuickSort".out1
java Main ./testCases/TreeVisitor.java 
clang-4.0 -o "LLVMfiles/TreeVisitor".out1 "LLVMfiles/TreeVisitor".ll
./"LLVMfiles/TreeVisitor".out1
java Main ./testCases/minijava-extra/Add.java 
clang-4.0 -o "LLVMfiles/Add".out1 "LLVMfiles/Add".ll
./"LLVMfiles/Add".out1
java Main ./testCases/minijava-extra/ArrayTest.java 
clang-4.0 -o "LLVMfiles/ArrayTest".out1 "LLVMfiles/ArrayTest".ll
./"LLVMfiles/ArrayTest".out1
java Main ./testCases/minijava-extra/CallFromSuper.java 
clang-4.0 -o "LLVMfiles/CallFromSuper".out1 "LLVMfiles/CallFromSuper".ll
./"LLVMfiles/CallFromSuper".out1
java Main ./testCases/minijava-extra/Classes.java 
clang-4.0 -o "LLVMfiles/Classes".out1 "LLVMfiles/Classes".ll
./"LLVMfiles/Classes".out1
java Main ./testCases/minijava-extra/DerivedCall.java 
clang-4.0 -o "LLVMfiles/DerivedCall".out1 "LLVMfiles/DerivedCall".ll
./"LLVMfiles/DerivedCall".out1
java Main ./testCases/minijava-extra/Example1.java 
clang-4.0 -o "LLVMfiles/Example1".out1 "LLVMfiles/Example1".ll
./"LLVMfiles/Example1".out1
java Main ./testCases/minijava-extra/FieldAndClassConflict.java 
clang-4.0 -o "LLVMfiles/FieldAndClassConflict".out1 "LLVMfiles/FieldAndClassConflict".ll
./"LLVMfiles/FieldAndClassConflict".out1
java Main ./testCases/minijava-extra/Main.java 
clang-4.0 -o "LLVMfiles/Main".out1 "LLVMfiles/Main".ll
./"LLVMfiles/Main".out1
java Main ./testCases/minijava-extra/ManyClasses.java 
clang-4.0 -o "LLVMfiles/ManyClasses".out1 "LLVMfiles/ManyClasses".ll
./"LLVMfiles/ManyClasses".out1
java Main ./testCases/minijava-extra/OutOfBounds1.java 
clang-4.0 -o "LLVMfiles/OutOfBounds1".out1 "LLVMfiles/OutOfBounds1".ll
./"LLVMfiles/OutOfBounds1".out1
java Main ./testCases/minijava-extra/Overload2.java 
clang-4.0 -o "LLVMfiles/Overload2".out1 "LLVMfiles/Overload2".ll
./"LLVMfiles/Overload2".out1
java Main ./testCases/minijava-extra/ShadowBaseField.java 
clang-4.0 -o "LLVMfiles/ShadowBaseField".out1 "LLVMfiles/ShadowBaseField".ll
./"LLVMfiles/ShadowBaseField".out1
java Main ./testCases/minijava-extra/ShadowField.java 
clang-4.0 -o "LLVMfiles/ShadowField".out1 "LLVMfiles/ShadowField".ll
./"LLVMfiles/ShadowField".out1
java Main ./testCases/minijava-extra/test06.java 
clang-4.0 -o "LLVMfiles/test06".out1 "LLVMfiles/test06".ll
./"LLVMfiles/test06".out1
java Main ./testCases/minijava-extra/test07.java 
clang-4.0 -o "LLVMfiles/test07".out1 "LLVMfiles/test07".ll
./"LLVMfiles/test07".out1
java Main ./testCases/minijava-extra/test15.java 
clang-4.0 -o "LLVMfiles/test15".out1 "LLVMfiles/test15".ll
./"LLVMfiles/test15".out1
java Main ./testCases/minijava-extra/test17.java 
clang-4.0 -o "LLVMfiles/test17".out1 "LLVMfiles/test17".ll
./"LLVMfiles/test17".out1
java Main ./testCases/minijava-extra/test20.java 
clang-4.0 -o "LLVMfiles/test20".out1 "LLVMfiles/test20".ll
./"LLVMfiles/test20".out1
java Main ./testCases/minijava-extra/test62.java 
clang-4.0 -o "LLVMfiles/test62".out1 "LLVMfiles/test62".ll
./"LLVMfiles/test62".out1
java Main ./testCases/minijava-extra/test73.java 
clang-4.0 -o "LLVMfiles/test73".out1 "LLVMfiles/test73".ll
./"LLVMfiles/test73".out1
java Main ./testCases/minijava-extra/test82.java 
clang-4.0 -o "LLVMfiles/test82".out1 "LLVMfiles/test82".ll
./"LLVMfiles/test82".out1
java Main ./testCases/minijava-extra/test93.java 
clang-4.0 -o "LLVMfiles/test93".out1 "LLVMfiles/test93".ll
./"LLVMfiles/test93".out1
java Main ./testCases/minijava-extra/test99.java
clang-4.0 -o "LLVMfiles/test99".out1 "LLVMfiles/test99".ll
./"LLVMfiles/test99".out1
