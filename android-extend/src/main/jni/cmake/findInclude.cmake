FUNCTION(INCLUDE_SUB_DIRECTORIES_RECURSIVELY root_dir)
    IF (IS_DIRECTORY ${root_dir})               # 当前路径是一个目录吗，是的话就加入到包含目录
        MESSAGE("include dir: " ${root_dir})
        INCLUDE_DIRECTORIES(${root_dir})
    ENDIF()

    FILE(GLOB ALL_SUB RELATIVE ${root_dir} ${root_dir}/*) # 获得当前目录下的所有文件，让如ALL_SUB列表中
    FOREACH(sub ${ALL_SUB})
        IF (IS_DIRECTORY ${root_dir}/${sub})                    
            INCLUDE_SUB_DIRECTORIES_RECURSIVELY(${root_dir}/${sub}) # 对子目录递归调用，包含
        ENDIF()
    ENDFOREACH()
ENDFUNCTION()

FUNCTION(ALL_SUB_DIRECTORIES_RECURSIVELY root_dir sub_dir)
    IF (IS_DIRECTORY ${root_dir})               # 当前路径是一个目录吗，是的话就加入到包含目录
        MESSAGE("add dir: " ${root_dir})
    ENDIF()

    FILE(GLOB ALL_SUB RELATIVE ${root_dir} ${root_dir}/*) # 获得当前目录下的所有文件，让如ALL_SUB列表中
    FOREACH(sub ${ALL_SUB})
        IF (IS_DIRECTORY ${root_dir}/${sub})                    
            ALL_SUB_DIRECTORIES_RECURSIVELY(${root_dir}/${sub} deep) # 对子目录递归调用，包含
			SET(deep_list ${deep_list} ${deep})
        ENDIF()
    ENDFOREACH()
	SET(${sub_dir} ${root_dir} ${deep_list} PARENT_SCOPE)
ENDFUNCTION()