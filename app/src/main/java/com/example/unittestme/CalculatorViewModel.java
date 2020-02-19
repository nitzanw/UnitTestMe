package com.example.unittestme;


import android.content.SharedPreferences;

import static com.example.unittestme.Constants.ADDED;

public class CalculatorViewModel {

    private SharedPreferences mSp;
    private MathServer mMathServer;


    CalculatorViewModel(SharedPreferences sp, MathServer mathServer) {

        mSp = sp;
        mMathServer = mathServer;
    }

    /**
     * Addition operation
     */
    public int add(int firstOperand, int secondOperand) {
        int result = firstOperand + secondOperand;
        int previous = mSp.getInt(ADDED, 0);
        if(previous == result){
            mMathServer.sendEqualToPrevious();
        }
        return result;
    }

    /**
     * Substract operation
     */
    public int sub(int firstOperand, int secondOperand) {
        return firstOperand - secondOperand;
    }


    /**
     * Multiply operation
     */
    public int mul(int firstOperand, int secondOperand) {
        int result = firstOperand * secondOperand;

        if (result > 0) {

            mMathServer.sendPositiveResults();

        } else if (result == 0) {
            mMathServer.sendZeroResults();
        }

        return result;
    }
}
