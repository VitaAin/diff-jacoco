#!/usr/bin/env bash

echo
echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< 开始 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


PREPARED=false


BUILD_DIFF_JACOCO=false
read -p "? 是否构建 diff-jacoco？ y(yes)/n(no)(default)    " ANSWER
if [ "$ANSWER" = y ]
then
  BUILD_DIFF_JACOCO=true
  echo "* 构建 diff-jacoco"
else
  BUILD_DIFF_JACOCO=false
  echo "* 不构建 diff-jacoco"
fi
#echo "* 是否构建 diff-jacoco： $BUILD_DIFF_JACOCO"

# diff-jacoco 根目录
DIFF_JACOCO_DIR=/Users/wangt/Workspace/VitaSpace/1/diff-jacoco
echo "* diff-jacoco 根目录： $DIFF_JACOCO_DIR"

# 构建的 org.jacoco.startup-0.8.4.tar.gz 路径
DIFF_JACOCO_JAR_NAME=org.jacoco.startup-0.8.4
DIFF_JACOCO_TARGET_DIR=$DIFF_JACOCO_DIR/org.jacoco.startup/target
DIFF_JACOCO_JAR_DIR=$DIFF_JACOCO_TARGET_DIR/$DIFF_JACOCO_JAR_NAME
DIFF_JACOCO_TAR_PATH=$DIFF_JACOCO_JAR_DIR.tar.gz
echo "* jacoco tar 路径： $DIFF_JACOCO_TAR_PATH"

if $BUILD_DIFF_JACOCO
then
  echo "================== 构建 diff-jacoco 开始 =================="
  # 打包
  mvn clean install -Dmaven.javadoc.test=true -Dmaven.test.skip=true
  echo "================== 构建 diff-jacoco 结束 =================="

  if [ -f $DIFF_JACOCO_TAR_PATH ]
  then
    echo "* 文件存在，构建成功!"

    echo "> 删除已存在的解压路径"
    rm -rf $DIFF_JACOCO_JAR_DIR
    echo "> 开始解压"
    # 解压 ./org.jacoco.startup/target/org.jacoco.startup-0.8.4.tar.gz
    tar zxvf $DIFF_JACOCO_TAR_PATH -C $DIFF_JACOCO_TARGET_DIR
    echo "> 结束解压"

    PREPARED=true
  else
    echo "* 文件不存在，构建失败！！！"
  fi
fi

if [ -d $DIFF_JACOCO_JAR_DIR ]
then
  echo "* tar 目录存在"
  PREPARED=true
else
  echo "* tar 目录不存在，请重新构建！！！"
fi


JACOCO_SH_PATH=$DIFF_JACOCO_JAR_DIR/bin/jacoco.sh
echo "* jacoco脚本路径：" $JACOCO_SH_PATH

if [ -f $JACOCO_SH_PATH ]
then
  echo "* jacoco脚本 文件存在"
  PREPARED=true
else
  echo "* jacoco脚本 文件不存在，请重新构建！！！"
fi


if $PREPARED
then
  echo
  echo "> 即将执行jacoco脚本"
  echo

#  CONF_FILE=$DIFF_JACOCO_DIR/exec.conf
  CONF_FILE=$DIFF_JACOCO_DIR/exec_cloud.conf
  . $CONF_FILE

  echo "* 项目根目录： $PROJECT_DIR"
  echo "* 项目modules： ${PROJECT_MODULES[*]}"
  PROJECT_MODULES_SIZE=${#PROJECT_MODULES[@]}
  echo "* 项目modules个数： $PROJECT_MODULES_SIZE"

  PATH_JAVA_CODE=src/main/java
  PATH_JAVA_CLASS=build/intermediates/javac/debug/classes
  PATH_KOTLIN_CLASS=build/tmp/kotlin-classes/debug

  for (( i = 0; i < PROJECT_MODULES_SIZE; i++ )); do
    if [ $i == 0 ]
    then
      PROJECT_SOURCE=$PROJECT_DIR/${PROJECT_MODULES[$i]}/$PATH_JAVA_CODE
      PROJECT_CLASS=$PROJECT_DIR/${PROJECT_MODULES[$i]}/$PATH_JAVA_CLASS,$PROJECT_DIR/${PROJECT_MODULES[$i]}/$PATH_KOTLIN_CLASS
    else
      PROJECT_SOURCE=$PROJECT_SOURCE,$PROJECT_DIR/${PROJECT_MODULES[$i]}/$PATH_JAVA_CODE
      PROJECT_CLASS=$PROJECT_CLASS,$PROJECT_DIR/${PROJECT_MODULES[$i]}/$PATH_JAVA_CLASS,$PROJECT_DIR/${PROJECT_MODULES[$i]}/$PATH_KOTLIN_CLASS
    fi
  done
#  echo "$PROJECT_SOURCE"
#  echo "$PROJECT_CLASS"


#  echo "* 项目源码目录： ${PROJECT_SOURCE_DIRS[*]}"
#  PROJECT_SOURCE_DIRS_SIZE=${#PROJECT_SOURCE_DIRS[@]}
#  echo "* 项目源码目录个数： $PROJECT_SOURCE_DIRS_SIZE"
#  echo "* 项目字节码目录： ${PROJECT_CLASS_DIRS[*]}"
#  PROJECT_CLASS_DIRS_SIZE=${#PROJECT_CLASS_DIRS[@]}
#  echo "* 项目字节码目录个数： $PROJECT_CLASS_DIRS_SIZE"
#
#  for (( i = 0; i < PROJECT_SOURCE_DIRS_SIZE; i++ )); do
#    if [ $i == 0 ]
#    then
#      PROJECT_SOURCE=${PROJECT_SOURCE_DIRS[$i]}
#    else
#      PROJECT_SOURCE=$PROJECT_SOURCE,${PROJECT_SOURCE_DIRS[$i]}
#    fi
#  done
##  echo "$PROJECT_SOURCE"
#
#  for (( i = 0; i < PROJECT_CLASS_DIRS_SIZE; i++ )); do
#    if [ $i == 0 ]
#    then
#      PROJECT_CLASS=${PROJECT_CLASS_DIRS[$i]}
#    else
#      PROJECT_CLASS=$PROJECT_CLASS,${PROJECT_CLASS_DIRS[$i]}
#    fi
#  done
##  echo "$PROJECT_CLASS"

#  echo "* .ec文件目录： $EXEC_PATH"
  echo "* .ec文件目录： ${EXEC_PATHS[*]}"
  echo "* 报告生成目录： $REPORT_DIR"

  EXEC_PATH_SIZE=${#EXEC_PATHS[@]}
  for (( i = 0; i < EXEC_PATH_SIZE; i++ )); do
    if [ $i == 0 ]
    then
      EXECS=${EXEC_PATHS[$i]}
    else
      EXECS=$EXECS,${EXEC_PATHS[$i]}
    fi
  done
#  echo "$EXECS"

  # Git 账户信息
  echo
  echo
  read -p "? 请输入Git用户名：    " ANSWER
  if [ -n "$ANSWER" ] ;then
      GIT_USER_NAME=$ANSWER
      echo "* Git用户名：$GIT_USER_NAME"
  else
      echo "* Git用户名为空！中断脚本！"
      exit
  fi
  read -s -p "? 请输入Git密码：    " ANSWER
  if [ -n "$ANSWER" ] ;then
      GIT_USER_PWD=$ANSWER
#      echo "* Git密码：$GIT_USER_PWD"
  else
      echo "* Git密码为空！中断脚本！"
      exit
  fi

  # 当前分支
  CURRENT_BRANCH=dev_2
  echo
  read -p "? 请输入当前分支名：    " ANSWER
  if [ -n "$ANSWER" ] ;then
      CURRENT_BRANCH=$ANSWER
      echo "* 当前分支名：$CURRENT_BRANCH"
  else
      echo "* 当前分支名为空！中断脚本！"
      exit
  fi
#  echo "* 当前分支： $CURRENT_BRANCH"


  DIFF_WHAT=1
  echo
  read -p "? 对比 Branch/Tag？： 1(branch)(default)/2(tag)    " ANSWER
  if [ "$ANSWER" = 2 ]
  then
    DIFF_WHAT=2
    echo "* 对比 Tag"
  else
    DIFF_WHAT=1
    echo "* 对比 Branch"
  fi

  LOGGABLE=true
#  LOGGABLE=false

  echo
  echo
  echo "================== 执行jacoco脚本 开始 =================="

  cd $DIFF_JACOCO_DIR

  # 执行 ./org.jacoco.startup/target/org.jacoco.startup-0.8.4/bin/jacoco.sh
  if [ $DIFF_WHAT == 1 ]
  then
    echo "> 对比分支"
    COMPARED_BRANCH=dev
    read -p "? 请输入要对比的分支名：    " ANSWER
    if [ -n "$ANSWER" ] ;then
        COMPARED_BRANCH=$ANSWER
        echo "* 要对比的分支名：$COMPARED_BRANCH"
    else
        echo "* 分支名为空！中断脚本！"
        exit
    fi

    echo "> 即将对比分支： $CURRENT_BRANCH 和 $COMPARED_BRANCH"
    echo
    $JACOCO_SH_PATH --loggable $LOGGABLE --git-work-dir $PROJECT_DIR --branch $CURRENT_BRANCH --compare-branch $COMPARED_BRANCH --report-dir $REPORT_DIR --exec-file-paths $EXECS --source-dirs $PROJECT_SOURCE --class-dirs $PROJECT_CLASS --git-user-name $GIT_USER_NAME --git-user-pwd $GIT_USER_PWD
  elif [ $DIFF_WHAT == 2 ]
  then
    echo "> 对比Tag"
    TAG=tag2
    COMPARED_TAG=tag1
    read -p "? 请输入要对比的Tag名：    " ANSWER
    if [ -n "$ANSWER" ] ;then
        TAG=$ANSWER
        echo "* Tag名1校验通过：$TAG"
    else
        echo "* Tag名1为空！中断脚本！"
        exit
    fi
    read -p "? 请输入要对比的Tag名：    " ANSWER
    if [ -n "$ANSWER" ] ;then
        COMPARED_TAG=$ANSWER
        echo "* Tag名2校验通过：$COMPARED_TAG"
    else
        echo "* Tag名2为空！中断脚本！"
        exit
    fi

    echo "> 即将对比Tag： $TAG 和 $COMPARED_TAG"
    echo
    $JACOCO_SH_PATH --loggable $LOGGABLE --git-work-dir $PROJECT_DIR --branch $CURRENT_BRANCH --tag $TAG --compare-tag $COMPARED_TAG --report-dir $REPORT_DIR --exec-file-paths $EXECS --source-dirs $PROJECT_SOURCE --class-dirs $PROJECT_CLASS --git-user-name $GIT_USER_NAME --git-user-pwd $GIT_USER_PWD
  else
    echo "* 参数错误！中断脚本！"
    exit
  fi

  echo "================== 执行jacoco脚本 结束 =================="
  echo

  echo "******* 请前往该路径查看报告：$REPORT_DIR/index.html"
  echo
fi


echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< 结束 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo
