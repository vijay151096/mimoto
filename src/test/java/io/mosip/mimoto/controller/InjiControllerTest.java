package io.mosip.mimoto.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import io.mosip.kernel.biometrics.spi.CbeffUtil;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.mimoto.TestBootApplication;
import io.mosip.mimoto.core.http.ResponseWrapper;
import io.mosip.mimoto.dto.mimoto.*;
import io.mosip.mimoto.dto.resident.*;
import io.mosip.mimoto.exception.ApisResourceAccessException;
import io.mosip.mimoto.exception.BaseUncheckedException;
import io.mosip.mimoto.model.Event;
import io.mosip.mimoto.model.EventModel;
import io.mosip.mimoto.service.RestClientService;
import io.mosip.mimoto.service.impl.CredentialShareServiceImpl;
import io.mosip.mimoto.util.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestBootApplication.class)
@AutoConfigureMockMvc
@PrepareForTest({Files.class})
public class InjiControllerTest {

    @InjectMocks
    private CommonInjiController commonInjiController = new CommonInjiController();

    @InjectMocks
    private CredentialShareController credentialShareController = new CredentialShareController();

    @MockBean
    private Map<String, String> injiConfig;

    @MockBean
    private CbeffUtil cbeffUtil;

    @MockBean
    private WebSubSubscriptionHelper webSubSubscriptionHelper;

    @MockBean
    private CredentialShareServiceImpl credentialShareService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttestationOfflineVerify attestationOfflineVerify;

    @MockBean
    private AttestationOnlineVerify attestationOnlineVerify;

    @MockBean
    public RestClientService<Object> restClientService;

    @MockBean
    public CryptoCoreUtil cryptoCoreUtil;

    @MockBean
    private Utilities utilities;

    @Before
    public void setup() throws IOException {
        //PowerMockito.mockStatic(Files.class);
        //PowerMockito.doNothing().when(Files.write(Mockito.any(Path.class), Mockito.any(byte[].class)));

        Map<String, String> injiConfig = new HashMap<>();
        injiConfig.put("datashareUrl", "http://www.mosipdatashare.com");

        ReflectionTestUtils.setField(commonInjiController, "injiConfig", injiConfig);
        ReflectionTestUtils.setField(credentialShareController, "partnerEncryptionKey", "partnerkey");
        ReflectionTestUtils.setField(credentialShareController, "partnerId", "partnerId");
        ReflectionTestUtils.setField(credentialShareController, "topic", "topic");


        Mockito.when(utilities.getDataPath()).thenReturn("target");

        Mockito.when(webSubSubscriptionHelper.subscribeEvent(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new SubscriptionChangeResponse());
        Mockito.doNothing().when(webSubSubscriptionHelper).initSubscriptions();
        Mockito.doNothing().when(webSubSubscriptionHelper).registerTopic(Mockito.any());
        Mockito.doNothing().when(webSubSubscriptionHelper).publish(Mockito.any(), Mockito.any());
    }

    @Test
    public void getAllPropertiesTest() throws Exception {
        this.mockMvc.perform(get("/allProperties").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void processOfflineTest() throws Exception {
        AttestationStatement attestationStatement = new AttestationStatement();
        Mockito.when(attestationOfflineVerify.parseAndVerify(Mockito.anyString())).thenReturn(attestationStatement);
        this.mockMvc.perform(post("/safetynet/offline/verify").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("offlineStr"))
                .andExpect(status().isOk());
    }

    @Test
    public void processOfflineFailureTest() throws Exception {
        AttestationStatement attestationStatement = new AttestationStatement();
        Mockito.when(attestationOfflineVerify.parseAndVerify(Mockito.anyString())).thenThrow(new Exception("Exception"));
        this.mockMvc.perform(post("/safetynet/offline/verify").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("offlineStr"))
                .andExpect(status().isOk());
    }

    @Test
    public void processOnlineTest() throws Exception {
        AttestationStatement attestationStatement = new AttestationStatement();
        Mockito.when(attestationOnlineVerify.parseAndVerify(Mockito.anyString())).thenReturn(attestationStatement);
        this.mockMvc.perform(post("/safetynet/online/verify").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("offlineStr"))
                .andExpect(status().isOk());
    }

    @Test
    public void processOnlineFailureTest() throws Exception {
        AttestationStatement attestationStatement = new AttestationStatement();
        Mockito.when(attestationOnlineVerify.parseAndVerify(Mockito.anyString())).thenThrow(new Exception("Exception"));
        this.mockMvc.perform(post("/safetynet/online/verify").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("offlineStr"))
                .andExpect(status().isOk());
    }

    /** CREDENTIAL SHARE CONTROLLER TEST CASES */

    @Test
    @Ignore
    public void handleSubscribeEventTest() throws Exception {
        Event event = new Event();
        event.setId("id");
        event.setTransactionId("transId");
        EventModel eventModel = new EventModel();
        eventModel.setEvent(event);

        Mockito.when(credentialShareService.generateDocuments(Mockito.any())).thenReturn(true);

        this.mockMvc.perform(post("/v1/mimoto/credentialshare/callback/notify").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(eventModel)))
                .andExpect(status().isOk());
    }

    @Test
    public void requestTest() throws Exception {
        AppCredentialRequestDTO requestDTO = new AppCredentialRequestDTO();
        requestDTO.setUser("mono");
        requestDTO.setTransactionID("transactionId");
        requestDTO.setOtp("123456");
        requestDTO.setIndividualId("123456");
        requestDTO.setCredentialType("VERCRED");

        CredentialRequestResponseInnerResponseDTO dto = new CredentialRequestResponseInnerResponseDTO();
        dto.setRequestId("requestId");
        CredentialRequestResponseDTO response = new CredentialRequestResponseDTO();
        response.setResponse(dto);

        Mockito.when(restClientService.postApi(Mockito.any(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);

        Mockito.when(credentialShareService.generateDocuments(Mockito.any())).thenReturn(true);

        this.mockMvc.perform(post("/credentialshare/request").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void requestStatusTest() throws Exception {

        ResponseWrapper<CredentialRequestStatusResponseDTO> responseWrapper = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(responseWrapper);

        this.mockMvc.perform(get("/credentialshare/request/status/1234567890").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void downloadTest() throws Exception {

        CredentialDownloadRequestDTO requestDTO = new CredentialDownloadRequestDTO();
        requestDTO.setRequestId("requestId");
        requestDTO.setIndividualId("individualId");

        JsonNode credentialJSON = JsonNodeFactory.instance.objectNode();
        Mockito.when(utilities.getVC(Mockito.anyString())).thenReturn(credentialJSON);

        JsonNode decryptedCredentialJSON = JsonNodeFactory.instance.objectNode();
        Mockito.when(utilities.getDecryptedVC(Mockito.anyString())).thenReturn(decryptedCredentialJSON);

        JsonNode requestCredentialJSON = JsonNodeFactory.instance.objectNode();
        Mockito.when(utilities.getRequestVC(Mockito.anyString())).thenReturn(requestCredentialJSON);

        this.mockMvc.perform(post("/credentialshare/download").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());

        Mockito.when(utilities.getDecryptedVC(Mockito.anyString())).thenReturn(null);

        this.mockMvc.perform(post("/credentialshare/download").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isNotFound());

        Mockito.when(utilities.getDecryptedVC(Mockito.anyString())).thenThrow(new IOException("Exception"));

        this.mockMvc.perform(post("/credentialshare/download").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    /** IDP CONTROLLER TEST CASES */

    @Test
    public void otpRequestTest() throws Exception {
        BindingOtpInnerReqDto innerReqDto = new BindingOtpInnerReqDto();
        innerReqDto.setIndividualId("individualId");
        innerReqDto.setOtpChannels(Lists.newArrayList("EMAIL"));
        BindingOtpRequestDto requestDTO = new BindingOtpRequestDto(DateUtils.getUTCCurrentDateTimeString(), innerReqDto);


        ResponseWrapper<BindingOtpResponseDto> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(response).thenThrow(new BaseUncheckedException("Exception"));

        this.mockMvc.perform(post("/binding-otp").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/binding-otp").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void walletBindingTest() throws Exception {
        WalletBindingInnerReq innerReqDto = new WalletBindingInnerReq();
        innerReqDto.setPublicKey("-----BEGIN RSA PUBLIC KEY-----\nMIICCgKCAgEAn6+frMlD7DQqbxZW943hRLBApDj1/lHIJdLYSKEGIfwhd58gc0Y4\n1q11mPnpv7gAZ/Wm0iOAkWSzcIWljXFmGnLrUrBsp4WYKdPjqn4tkrCOjiZa5RPk\nY03a40Kz1lx0W9f94Naozglf6KFUSq+qAwuC5kiPxaxsjFA/LWIP+zT2QX/MnrX9\nv7gt2g0BC4pQ01eTTzhhwO2A7k5z3ucsb56ohND4xdIsdCMm1IczBjW0URSO60Bb\n7m5dlO8BFHJ6inV8awO2KHoADbp3wZgid4KqLJ0eVGyNViVFzj4rxSxL3vcYbyKS\nORWSlPZIZL9ZWO1cyPO9+Wxu29IKj4DQEt8glgITlBZ4L29uT7gFPAbypSn/8SvU\nBrNno8+GIe9XWsrDTMT9dfLGzLUitF3A+wwVZuRVhCqYIisOOGuGE18YK0jmdk9l\n89OpK4PduGiUh66zZTcH3thdtaOz6jj+FLKMg2Q3gNqQ1Y0cezO175RNVVX1ffOu\n5qss1RWams5RAXDqqt/MhiopG3DhlyaSC4xdqei7SI8d+S4Bvflub9rypPnhW67g\nNhZvQDJ7Tb1AWHxKmU0wQvEMtwSm9xtsMs4bqotn2M/09BuRqbrhpvAfrfZArkVO\nv8eLXhtDvo2J9gRwHZIS/JZ1Fo+tep1QFHz1Lr5iGRqwLWQlGbKFuL0CAwEAAQ==\n-----END RSA PUBLIC KEY-----\n");
        innerReqDto.setIndividualId("individualId");
        WalletBindingRequestDTO requestDTO = new WalletBindingRequestDTO();
        requestDTO.setRequestTime(DateUtils.getUTCCurrentDateTimeString());
        requestDTO.setRequest(innerReqDto);

        ResponseWrapper<WalletBindingResponseDto> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(response).thenThrow(new BaseUncheckedException("Exception"));

        this.mockMvc.perform(post("/wallet-binding").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/wallet-binding").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @Ignore
    public void linkTransactionTest() throws Exception {
        LinkTransactionRequestDto requestDTO = new LinkTransactionRequestDto();
        requestDTO.setRequestTime(DateUtils.getUTCCurrentDateTimeString());
        requestDTO.setLinkCode("linkcode");

        LinkTransactionResponseDto response = new LinkTransactionResponseDto();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(response).thenThrow(new BaseUncheckedException("Exception"));

        this.mockMvc.perform(post("/link-transaction").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/link-transaction").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @Ignore
    public void idpAuthAndConsentTest() throws Exception {
        AuthAndConsentRequestDto innerDto = new AuthAndConsentRequestDto();
        innerDto.setLinkedTransactionId("LinkedTransactionId");
        innerDto.setIndividualId("individualId");
        innerDto.setLinkedTransactionId("LinkedTransactionId");
        innerDto.setAcceptedClaims(Lists.newArrayList("OTP"));
        IdpAuthChallangeDto challangeDto = new IdpAuthChallangeDto();
        challangeDto.setAuthFactorType("OTP");
        challangeDto.setChallenge("1234");
        innerDto.setChallengeList(Lists.newArrayList(challangeDto));
        IdpAuthAndConsentDto requestDto = new IdpAuthAndConsentDto();
        requestDto.setRequestTime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setRequest(innerDto);


        LinkedTransactionResponseDto resp = new LinkedTransactionResponseDto();
        resp.setLinkedTransactionId("linkedTransactionId");
        ResponseWrapper<LinkedTransactionResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(resp);

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(responseWrapper)
                .thenReturn(responseWrapper).thenThrow(new ApisResourceAccessException("exception"));

        this.mockMvc.perform(post("/idp-auth-consent").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JsonUtils.javaObjectToJsonString(requestDto)))
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/idp-auth-consent").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JsonUtils.javaObjectToJsonString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @Ignore
    public void idpAuthenticateTest() throws Exception {
        IdpAuthInternalRequestDto innerDto = new IdpAuthInternalRequestDto();
        innerDto.setLinkedTransactionId("LinkedTransactionId");
        innerDto.setIndividualId("individualId");
        IdpAuthChallangeDto challangeDto = new IdpAuthChallangeDto();
        challangeDto.setAuthFactorType("OTP");
        challangeDto.setChallenge("1234");
        innerDto.setChallengeList(Lists.newArrayList(challangeDto));
        IdpAuthRequestDto requestDto = new IdpAuthRequestDto();
        requestDto.setRequestTime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setRequest(innerDto);


        BindingOtpResponseDto responseDto = new BindingOtpResponseDto();
        responseDto.setTransactionId("1111111");
        ResponseWrapper<BindingOtpResponseDto> otpResponse = new ResponseWrapper<>();
        otpResponse.setResponse(responseDto);
        ResponseWrapper<BindingOtpResponseDto> otpResponse2 = new ResponseWrapper<>();
        ResponseWrapper<LinkedTransactionResponseDto> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(otpResponse).thenReturn(response).thenReturn(otpResponse2);

        this.mockMvc.perform(post("/idp-authenticate").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDto)))
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/idp-authenticate").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @Ignore
    public void idpConsentTest() throws Exception {
        IdpConsentRequestDto innerDto = new IdpConsentRequestDto();
        innerDto.setLinkedTransactionId("LinkedTransactionId");
        innerDto.setAcceptedClaims(Lists.newArrayList("OTP"));
        IdpConsentDto requestDto = new IdpConsentDto();
        requestDto.setRequestTime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setRequest(innerDto);


        ResponseWrapper<LinkedTransactionResponseDto> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(response).thenThrow(new BaseUncheckedException("Exception"));

        this.mockMvc.perform(post("/idp-consent").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDto)))
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/idp-consent").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDto)))
                .andExpect(status().isOk());
    }

    /** RESIDENT CONTROLLER TEST CASES */
    @Test
    public void residentOtpRequestTest() throws Exception {
        AppOTPRequestDTO requestDTO = new AppOTPRequestDTO();
        requestDTO.setIndividualId("IndividualId");
        requestDTO.setIndividualIdType("UIN");
        requestDTO.setTransactionID("1234567890");
        requestDTO.setOtpChannel(Lists.newArrayList("EMAIL"));


        ResponseWrapper<CredentialRequestResponseDTO> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(response);

        this.mockMvc.perform(post("/req/otp").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void vidGenerateTest() throws Exception {
        AppVIDGenerateRequestDTO requestDTO = new AppVIDGenerateRequestDTO();
        requestDTO.setIndividualId("IndividualId");
        requestDTO.setIndividualIdType("UIN");
        requestDTO.setTransactionID("1234567890");
        requestDTO.setOtp("111111");


        ResponseWrapper<VIDGeneratorResponseDTO> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);

        this.mockMvc.perform(post("/vid").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void authLockTest() throws Exception {
        AuthLockRequestDTO requestDTO = new AuthLockRequestDTO();
        requestDTO.setIndividualId("IndividualId");
        requestDTO.setIndividualIdType("UIN");
        requestDTO.setTransactionID("1234567890");
        requestDTO.setOtp("111111");


        ResponseWrapper<AuthLockUnlockResponseDTO> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);

        this.mockMvc.perform(post("/req/auth/lock").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void authUnLockTest() throws Exception {
        AuthUnlockRequestDTO requestDTO = new AuthUnlockRequestDTO();
        requestDTO.setIndividualId("IndividualId");
        requestDTO.setIndividualIdType("UIN");
        requestDTO.setTransactionID("1234567890");
        requestDTO.setOtp("111111");


        ResponseWrapper<AuthLockUnlockResponseDTO> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);

        this.mockMvc.perform(post("/req/auth/unlock").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void individualIdOtpRequestTest() throws Exception {
        IndividualIdOtpRequestDTO requestDTO = new IndividualIdOtpRequestDTO();
        requestDTO.setAid("IndividualId");
        requestDTO.setOtpChannel(Lists.newArrayList("EMAIL"));
        requestDTO.setTransactionID("1234567890");

        OTPResponseDTO response = new OTPResponseDTO();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);

        this.mockMvc.perform(post("/req/individualId/otp").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void aidGetIndividualIdTest() throws Exception {
        AidStatusRequestDTO requestDTO = new AidStatusRequestDTO();
        requestDTO.setAid("IndividualId");
        requestDTO.setTransactionID("1234567890");
        requestDTO.setOtp("111111");

        ResponseWrapper<AidStatusResponseDTO> response = new ResponseWrapper<>();

        Mockito.when(restClientService.postApi(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);

        this.mockMvc.perform(post("/aid/get-individual-id").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtils.javaObjectToJsonString(requestDTO)))
                .andExpect(status().isOk());
    }
}
