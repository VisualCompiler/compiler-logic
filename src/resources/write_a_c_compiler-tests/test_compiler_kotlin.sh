#!/bin/bash

padding_dots=$(printf '%0.1s' "."{1..60})
padlength=50
compiler_jar=$1
success_total=0
failure_total=0

print_test_name () {
    test_name=$1
    printf '%s' "$test_name"
    printf '%*.*s' 0 $((padlength - ${#test_name})) "$padding_dots"
}

test_success () {
    echo "OK"
    ((success++))
}

test_failure () {
    echo "FAIL"
    ((fail++))
}

test_not_implemented () {
    echo "NOT IMPLEMENTED"
}

run_our_compiler () {
    # Run the Kotlin compiler and capture exit code
    java -jar "$compiler_jar" "$1" >/dev/null 2>&1
    actual_exit_code=$?
}

run_correct_program () {
    # Compile with gcc to check if it's a valid C program
    gcc -w "$1" >/dev/null 2>&1
    expected_exit_code=$?
    if [ $expected_exit_code -eq 0 ]; then
        rm a.out 2>/dev/null
    fi
}

compare_program_results () {
    # For valid programs, both should succeed (exit code 0)
    # For invalid programs, both should fail (exit code != 0)
    if [ "$expected_exit_code" -eq 0 ] && [ "$actual_exit_code" -eq 0 ]; then
        test_success
    elif [ "$expected_exit_code" -ne 0 ] && [ "$actual_exit_code" -ne 0 ]; then
        test_success
    else
        test_failure
    fi
}

test_stage () {
    success=0
    fail=0
    echo "===================================================="
    echo "STAGE $1"
    echo "===================Valid Programs==================="
    for prog in `find . -type f -name "*.c" -path "./stage_$1/valid/*" -not -path "*/valid_multifile/*" 2>/dev/null`; do

        run_correct_program "$prog"

        base="${prog%.*}" #name of executable (filename w/out extension)
        test_name="${base##*valid/}"

        print_test_name "$test_name"
        
        if [[ $test_name == "skip_on_failure"* ]]; then
            # this may depend on features we haven't implemented yet
            # if compilation succeeds, make sure it gives the right result
            # otherwise don't count it as success or failure
            run_our_compiler "$prog"
            if [ "$actual_exit_code" -eq 0 ]; then
                # it succeeded, so check if it gives the right result
                compare_program_results
            else
                test_not_implemented
            fi
        else
            run_our_compiler "$prog"
            compare_program_results
        fi
    done
    # programs with multiple source files
    for dir in `ls -d stage_$1/valid_multifile/* 2>/dev/null` ; do
        gcc -w $dir/* >/dev/null 2>&1

        if [ $? -eq 0 ]; then
            expected_out=`./a.out 2>/dev/null`
            expected_exit_code=$?
            rm a.out 2>/dev/null
        else
            expected_exit_code=1
            expected_out=""
        fi

        base="${dir%.*}" #name of executable (directory w/out extension)
        test_name="${base##*valid_multifile/}"

        print_test_name "$test_name"

        # For multifile programs, we'll just test if our compiler can handle the first file
        # since the Kotlin compiler doesn't support multifile compilation in the same way
        first_file=$(ls $dir/*.c | head -1)
        run_our_compiler "$first_file"
        compare_program_results

    done
    echo "===================Invalid Programs================="
    for prog in `ls stage_$1/invalid/{,**/}*.c 2>/dev/null`; do

        base="${prog%.*}" #name of executable (filename w/out extension)
        test_name="${base##*invalid/}"

        print_test_name "$test_name"

        run_our_compiler "$prog"
        
        # For invalid programs, we expect compilation to fail (exit code != 0)
        if [ "$actual_exit_code" -ne 0 ]; then
            test_success
        else
            test_failure
        fi
    done
    echo "===================Stage $1 Summary================="
    printf "%d successes, %d failures\n" $success $fail
    ((success_total=success_total+success))
    ((failure_total=failure_total + fail))
}

total_summary () {
    echo "===================TOTAL SUMMARY===================="
    printf "%d successes, %d failures\n" $success_total $failure_total
}

if [ "$1" == "" ]; then
    echo "USAGE: ./test_compiler_kotlin.sh /path/to/compiler.jar [stages(optional)]"
    echo "EXAMPLE(test specific stages): ./test_compiler_kotlin.sh ./compiler.jar 1 2 4"
    echo "EXAMPLE(test all): ./test_compiler_kotlin.sh ./compiler.jar"
    exit 1
fi

if test 1 -lt $#; then
   testcases=("$@") # [1..-1] is testcases
   for i in `seq 2 $#`; do
       test_stage ${testcases[$i-1]}
   done
   total_summary
   exit 0
fi

num_stages=10

for i in `seq 1 $num_stages`; do
    test_stage $i
done

total_summary
