package com.retrofitstudy.common.data;

import java.util.Date;
import java.util.Random;

public class DefaultTokenGenerator implements ITokenGenerator{
    @Override
    public String geneatorToken() {
        int max = 30000;
        int min = 0;
        int diff = new Random().nextInt(max) % (max - min + 1) + min;
        return String.valueOf(new Date().getTime() - diff);
    }
}
