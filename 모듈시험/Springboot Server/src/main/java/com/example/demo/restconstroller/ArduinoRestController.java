package com.example.demo.restconstroller;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@RestController
@Slf4j
@RequestMapping("/arduino")
public class ArduinoRestController {
    //----------------------------------------------------------------
    // com.fazecast.jSerialComm.SerialPort 클래스를 가져와서 아두이노와 연결하는데 사용합니다.
    // 시리얼 포트 변수 및 로그 메시지 문자열 변수를 정의합니다.
    //----------------------------------------------------------------
    private SerialPort serialPort;
    private OutputStream outputStream;
    private InputStream inputStream;

    private String LedLog;
    private String TmpLog;
    private String LightLog;
    private String DistanceLog;

    // 연결 설정 메서드: /connection/{COM} 경로로 들어오는 GET 요청을 처리합니다.
    @GetMapping("/connection/{COM}")
    public ResponseEntity<String> setConnection(@PathVariable("COM") String COM, HttpServletRequest request) {
        log.info("GET /arduino/connection " + COM + " IP: " + request.getRemoteAddr());

        // 시리얼 포트가 이미 열려 있다면 닫습니다.
        if (serialPort != null) {
            serialPort.closePort();
            serialPort = null;
        }

        // 지정된 COM 포트와 연결합니다.
        serialPort = SerialPort.getCommPort(COM); 

        // 연결 설정을 구성합니다.
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(0);
        serialPort.setParity(0);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

        //----------------------------------------------------------------
        // 시리얼 포트와의 연결 유무를 확인하고, 연결이 성공했을 때와 실패했을 때를 처리합니다.
        //----------------------------------------------------------------
        boolean isOpen = serialPort.openPort();
        log.info("isOpen ? " + isOpen);

        if (isOpen) {
            this.outputStream = serialPort.getOutputStream();
            this.inputStream = serialPort.getInputStream();

            //----------------------------------------------------------------
            // 수신 스레드를 시작합니다.
            //----------------------------------------------------------------
            Worker worker = new Worker();
            Thread th = new Thread(worker);
            th.start();

            return new ResponseEntity("Connection Success!", HttpStatus.OK);
        } else {
            return new ResponseEntity("Connection Fail...!", HttpStatus.BAD_GATEWAY);
        }
    }

    //----------------------------------------------------------------
    // LED ON/OFF를 제어하는 코드로, value 값에 따라 LED 상태가 결정됩니다.
    //----------------------------------------------------------------
    @GetMapping("/led/{value}")
    public void led_Control(@PathVariable String value, HttpServletRequest request) throws IOException {
        log.info("GET /arduino/led/value : " + value + " IP : " + request.getRemoteAddr());
        if (serialPort.isOpen()) {
            outputStream.write(value.getBytes());
            outputStream.flush();
        }
    }

    //----------------------------------------------------------------
    // LED, 온도, 조도, 초음파 거리 정보를 제공하는 코드입니다.
    //----------------------------------------------------------------
    @GetMapping("/message/led")
    public String led_Message() throws InterruptedException {
        return LedLog;
    }

    @GetMapping("/message/tmp")
    public String tmp_Message() {
        return TmpLog;
    }

    @GetMapping("/message/light")
    public String light_Message() {
        return LightLog;
    }

    @GetMapping("/message/distance")
    public String distance_Message() {
        return DistanceLog;
    }

    //----------------------------------------------------------------
    // 수신 스레드 클래스: 아두이노에서 받은 정보를 처리하는 코드입니다.
    // 해당 코드가 배열 타입으로 자료를 받아와서 나열하고, 예외 처리를 통해 오류 발생 시 대비합니다.
    // 예를 들어, 배열의 0번째에 LEDLOG가 없으면 온도 로그를 놓습니다.
    //----------------------------------------------------------------
    class Worker implements Runnable {
        DataInputStream din;
        @Override
        public void run() {
            din = new DataInputStream(inputStream);
            try {
                while (!Thread.interrupted()) {
                    if (din != null) {
                        String data = din.readLine();
                        System.out.println("[DATA] : " + data);
                        String[] arr = data.split("_"); // LED, TMP, LIGHT, DIS

                        try {
                            if (arr.length > 3) {
                                LedLog = arr[0];
                                TmpLog = arr[1];
                                LightLog = arr[2];
                                DistanceLog = arr[3];
                            } else {
                                TmpLog = arr[0];
                                LightLog = arr[1];
                                DistanceLog = arr[2];
                            }
                        } catch (ArrayIndexOutOfBoundsException e1) {
                            e1.printStackTrace();
                        }
                    }
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //----------------------------------------------------------------
}