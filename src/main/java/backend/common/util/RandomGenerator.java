package backend.common.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomGenerator {

    private static final Random random=new Random();
    public int makeRandomNumber(){
        return random.nextInt(888888)+111111;
    }
}

