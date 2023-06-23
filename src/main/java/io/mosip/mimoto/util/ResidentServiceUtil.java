package io.mosip.mimoto.util;

import io.mosip.mimoto.dto.mimoto.AppOTPRequestDTO;
import io.mosip.mimoto.exception.InvalidInputException;
import io.mosip.mimoto.exception.PlatformErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResidentServiceUtil {

    private static Logger logger = LoggerFactory.getLogger(ResidentServiceUtil.class);

    @Value("${mosip.notificationtype}")
    private static String notificationType;

    public static void validateInputRequest(AppOTPRequestDTO requestDTO){
        List<String> incorrectNotificationChannel = new ArrayList<String>();
        String notificationChannels = notificationType;
        logger.info("\n Notification Channel from application-default.properties in mosip-config - > " + notificationChannels);
        logger.info("\n Notification Types from application-default.properties in mosip-config - > " + notificationType);
        incorrectNotificationChannel = requestDTO.getOtpChannel()
                .stream()
                .filter(otpChannel -> !notificationChannels.contains(otpChannel))
                .collect(Collectors.toList());
        if(incorrectNotificationChannel.size() > 0) {
            throw new InvalidInputException(PlatformErrorMessages.MIMOTO_PGS_INVALID_INPUT_PARAMETER.getMessage());
        }
    }
}
