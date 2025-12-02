package com.alextim.glagol;

import com.alextim.glagol.service.CanService;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CanService canService = new CanService();

        System.out.println("SEND");
        canService.startMeas();

        Thread.sleep(TimeUnit.MINUTES.toMillis(30));

        System.out.println("SHOT DOWN");
        canService.shutDown();

        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        System.out.println("END");
    }

}
