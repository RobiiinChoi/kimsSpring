package hello.hellospring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
/*
*   AOP의 장점 : 핵심 관심사항 <=> 공통 관심사항 분리
*   핵심 관심사항을 깔끔하게 유지 가능하고, 변경도 여기서만 변경 가능
*   원하는 적용대상을 선택할 수 있음
* */
@Aspect
@Component
public class TimeTraceApp {
    @Around("execution(* hello.hellospring..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        System.out.println("start : " +joinPoint.toString());
        try{
            return joinPoint.proceed();
        } finally {
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            System.out.println("end : " +joinPoint.toString()+ " " + timeMs + "Ms");
        }
    }
}
