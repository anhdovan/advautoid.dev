cmake_minimum_required(VERSION 3.15)
project(AutoIdCppSample VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_executable(autoid_sample
    main.cpp
    AdvSmartSdk.h
)

if (MSVC)
    target_compile_options(autoid_sample PRIVATE /W4 /EHsc /utf-8)
else()
    target_compile_options(autoid_sample PRIVATE -Wall -Wextra -pthread)
    target_link_libraries(autoid_sample PRIVATE dl pthread)
endif()
