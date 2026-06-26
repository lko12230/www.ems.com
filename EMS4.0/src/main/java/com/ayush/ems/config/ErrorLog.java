//package com.ayush.ems.config;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.stereotype.Component;
//
//@Component
//@Aspect
//public class ErrorLog {
//	@Before("execution(* com.ayush.ems.service..*(..))")
//	  public void logBefore() {
//	        System.out.println("📌 [LOG] Method is going to execute...");
//	    }
//	
//	@After("execution(* com.ayush.ems.service..*(..))")
//	  public void logAfter() {
//		System.out.println("✅ [LOG] Method execution finished.");
//	    }
//	
//    // 3. AfterReturning advice
//    @AfterReturning(pointcut = "execution(* com.ayush.ems.service..*(..))", returning = "result")
//    public void logAfterReturning(Object result) {
//        System.out.println("🎯 [LOG] Method returned successfully → " + result);
//    }
//    
// // 4. AfterThrowing advice
//    @AfterThrowing(pointcut = "execution(* com.ayush.ems.service..*(..))", throwing = "ex")
//    public void logAfterThrowing(Exception ex) {
//        System.out.println("❌ [LOG] Exception occurred → " + ex.getMessage());
//    }
//    
//    // 5. Around advice
//    @Around("execution(* com.ayush.ems.service..*(..))")
//    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
//        System.out.println("⏳ [LOG] Around (Before) → " + joinPoint.getSignature());
//        Object result;
//        try {
//            result = joinPoint.proceed(); // actual method call
//            System.out.println("✅ [LOG] Around (After Returning)");
//        } catch (Exception ex) {
//            System.out.println("🔥 [LOG] Around (Exception) → " + ex.getMessage());
//            throw ex;
//        }
//        System.out.println("🏁 [LOG] Around (After Finally)");
//        return result;
//    }
//}
