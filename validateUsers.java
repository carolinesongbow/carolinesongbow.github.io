@RequestMapping(value = "/app/appserver/uauth/validateUsers", method = RequestMethod.PUT)
    public Map<String, Object> validateUsers(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId")) || StringUtils.isEmpty(params.get("password"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号或登录密码不能为空");
        }
        if (StringUtils.isEmpty(params.get("deviceId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户端id不能为空");
        }
        String userId = (String) params.get("userId");
        String userPass = (String) params.get("password");
        String deviceId = (String) params.get("deviceId");
        String deviceType = (String) params.get("deviceType");

        if (!"IOS".equals(deviceType) && !"AND".equals(deviceType)) {
            return fail("10", "设备类型错误");
        }
        Map<String, Object> resultMap = new HashMap<>();


        String channel = super.getChannel();
        String channelNO = super.getChannelNO();
        if (StringUtils.isEmpty(channel)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "channel不能为空");
        }
        if (channel.equals("16")) {
            // 星巢贷订单走原接口 统一认证3接口
            resultMap = uauthService.validateUsers(params);
        } else if (channel.equals("14")) {
            // 个人版
            if (StringUtils.isEmpty(params.get("type"))) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "type不能为空");
            }
            String type = (String) params.get("type");
            // 首次登录，调用统一认证25接口
            if (type.equals("login")) {
                resultMap = uauthService.validateUsersCount(params);
            } else if (type.equals("set")) {
                // 登录后设置，调用统一认证30接口
                resultMap = uauthService.validateUsersHaier(params);
            } else {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "不支持的类型");
            }
        } else if ("34".equals(channelNO)) {
            // H5，调用统一认证29接口
            if (StringUtils.isEmpty(params.get("externUid"))) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "集团用户Id不能为空");
            }
            resultMap = uauthService.validateAndBindHaierUser(params);
        } else {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "不支持的渠道");
        }

        if (resultMap == null || resultMap.isEmpty()) {
            return fail(ConstUtil.ERROR_AUTH_FAIL_CODE, "统一认证系统通信失败");
        } else if (!super.isSuccess(resultMap)) {
            // 登录失败
            return resultMap;
        } else {
            // 存储登录信息
            // 数据解密
            userId = EncryptUtil.simpleDecrypt(userId);
            deviceId = EncryptUtil.simpleDecrypt(deviceId);

            // 生成clientSecret，并将deviceId和clientSecret保存到关系表
            PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
            String clientSecret = PASSWORD_ENCODER.encode(deviceId + userId);

            loginController.storeToken((String) ((Map<String, Object>) resultMap.get("body")).get("userId"), deviceId, clientSecret,
                    deviceType);
            ((Map<String, Object>) resultMap.get("body")).put("clientSecret", clientSecret);
            return resultMap;
        }

    }
