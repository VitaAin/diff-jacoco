package org.jacoco.core.diff;


import org.jacoco.core.internal.diff.CodeDiff;

public class CodeDiffTest {


    public static void main(String[] args) {
        String gitWorkDir = "/Users/wangt/Workspace/Test/JacocoDemo";
        String newBranchName = "dev_3";
        String oldBranchName = "dev_2";
        CodeDiff.diffBranchToBranch(gitWorkDir, newBranchName, oldBranchName);
    }

}
