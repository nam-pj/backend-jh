package org.example.board.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class WordValidationService {

    @Value("${korean.dict.api-key}")
    private String apiKey;

    public boolean isValidWord(String word) {

        // 1. 초성/자음/모음만 있는 경우 즉시 차단
        if (isOnlyConsonantOrVowel(word)) {
            return false;
        }

        // 2. 완성형 한글이 아닌 문자 포함 시 차단
        if (!word.matches("^[가-힣]+$")) {
            return false;
        }

        try {
            String encodedWord = URLEncoder.encode(word, "UTF-8");
            String apiUrl = "https://opendict.korean.go.kr/api/search"
                    + "?key=" + apiKey
                    + "&q=" + encodedWord
                    + "&target=1"
                    + "&part=word"
                    + "&num=10"
                    + "&type1=word"; // 단어 타입만 검색

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) return true;

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8")
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String response = sb.toString();
            System.out.println("API 응답: " + response);

            // total이 0이면 바로 false
            if (response.contains("<total>0</total>")) {
                return false;
            }

            // 표제어(<item>의 <word> 태그)가 입력한 단어와 정확히 일치하는지 확인
            // XML에서 <word>헬로</word> 형태로 반환됨
            String targetTag = "<word>" + word + "</word>";
            boolean exactMatch = response.contains(targetTag);

            System.out.println("정확히 일치하는 단어 존재: " + exactMatch);

            return exactMatch;

        } catch (Exception e) {
            System.out.println("API 호출 에러: " + e.getMessage());
            return true;
        }
    }

    private boolean isOnlyConsonantOrVowel(String word) {
        for (char c : word.toCharArray()) {
            if (c >= 0x3131 && c <= 0x3163) {
                return true;
            }
        }
        return false;
    }
}