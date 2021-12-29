---
layout: post
title:  "Springboot下拦截器实现"  
date:   2017-03-24 10:06:54 +0800  
tags: [Java, Springboot, AOP]
---  


1. 新建一个注释类  
```java  
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestCheck {
    boolean value() default true;
}  
```  

2. 新建一个拦截器类  
```java  
@Aspect
@Component
public class RequestInterceptor {
    public Log logger = LogFactory.getLog(this.getClass());

    // 拦截开关
    @Value("${common.app.checkAuth}")
    private Boolean checkAuth;

    private static final byte[] userStore = RedisUtil.serialize("__token_app_userStore");

    @Pointcut("execution(* com.mytest..*.*(..)) && @annotation(com.mytest.app.util.annotation.RequestCheck)")
    private void requestNeedCheck() {

    }

    @Before("requestNeedCheck()")
    public void before(JoinPoint joinPoint) throws Throwable {
        if (!checkAuth) {
            return;
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String userId = request.getParameter("userId");
        String token = request.getHeader("access_token");
        Object[] args = joinPoint.getArgs();
        logger.info("拦截器userId:" + userId + ", token:" + token);
        if (!StringUtils.isEmpty(token)) {
            byte[] key = RedisUtil.serialize(token);
            String redisUserId = (String) RedisUtil.unserialize(RedisUtil.hget(this.userStore, key));
            logger.info("拦截器redisUserId:" + redisUserId);
            if (!StringUtils.isEmpty(userId)) {
                if (!userId.equals(redisUserId)) {
                    throw new RuntimeException("非法请求:用户token与用户id校验失败");
                }
            } else {
                for (Object arg : args) {
                    if (arg instanceof Map) {
                        Map<String, Object> params = (Map<String, Object>) arg;
                        String var1 = String.valueOf(params.get("userId"));
                        String var2 = String.valueOf(params.get("user_id"));
                        logger.info("拦截器var1:" + var1 + ", var2:" + var2);
                        if (!"null".equals(var1.toLowerCase()) && !StringUtils.isEmpty(var1)) {
                            if (!var1.equals(redisUserId)) {
                                throw new RuntimeException("非法请求:用户token与用户id校验失败");
                            }
                        }
                        if (!"null".equals(var2.toLowerCase()) && !StringUtils.isEmpty(var1)) {
                            if (!var2.equals(redisUserId)) {
                                throw new RuntimeException("非法请求:用户token与用户id校验失败");
                            }
                        }

                    }
                }
            }

        }
    }
}
```  

3. 使用注解拦截方法  
```java  
@RequestMapping("/test")
@RequestCheck
public Map<String, Object> test(String test) {
    return success();
}  
```  
