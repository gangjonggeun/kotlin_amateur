package com.example.kotlin_amateur.core;

import java.util.ArrayList;
import java.util.List;

class zxc {
    public int[] solution(int []arr) {

        int same = arr[0];

        List<Integer> answerList = new ArrayList<>();
        answerList.add(same);

        for(int i=1; i<arr.length; i++){
            if(same != arr[i]){
                answerList.add(arr[i]);
                same = arr[i];
            }
        }

        int[] answer = new int[answerList.size()];

        for(int i=0; i<answerList.size(); i++){
            answer[i] = answerList.get(i);
        }

        return answer;
    }
}