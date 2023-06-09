package org.jacoco.core.diff;


import org.jacoco.core.internal.diff.ASTGenerator;
import org.jacoco.core.internal.diff.KotlinASTGenerator;

import java.util.ArrayList;

public class ASTGeneratorTest {



    public static void main(String[] args) {
//        ASTGenerator astGenerator = new ASTGenerator(Data.CODE_KT);
//        astGenerator.getClassInfo(Data.KT_PATH);

        KotlinASTGenerator generator = new KotlinASTGenerator(Data.CODE_KT, Data.KT_PATH);
        generator.getClassInfo(Data.KT_PATH, new ArrayList<>(), new ArrayList<>());

        KotlinASTGenerator generator2 = new KotlinASTGenerator(Data.CODE_KT_2, Data.KT_PATH_2);
        generator2.getClassInfo(Data.KT_PATH_2);
    }

}
