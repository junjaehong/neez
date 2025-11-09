package com.bbey.neez.service.Company;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DartCorpCodeService {

    @Value("${dart.api-key}")
    private String dartApiKey;

    // 회사명 -> corp_code
    private final Map<String, String> nameToCorpCode = new HashMap<>();

    private boolean loaded = false;

    public synchronized void loadIfNeeded() {
        if (loaded) return;

        try {
            // 1) zip 다운로드
            String urlStr = "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key=" + dartApiKey;
            URL url = new URL(urlStr);
            File tempZip = Files.createTempFile("dart-corp", ".zip").toFile();
            try (InputStream is = url.openStream();
                 FileOutputStream fos = new FileOutputStream(tempZip)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
            }

            // 2) zip 안의 xml 꺼내기
            File xmlFile = Files.createTempFile("dart-corp", ".xml").toFile();
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZip))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".xml")) {
                        try (FileOutputStream fos = new FileOutputStream(xmlFile)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        break;
                    }
                }
            }

            // 3) xml 파싱해서 map에 넣기
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc;
            try (FileInputStream fis = new FileInputStream(xmlFile)) {
                doc = builder.parse(fis);
            }
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("list");
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);
                NodeList children = node.getChildNodes();
                String corpCode = null;
                String corpName = null;
                for (int j = 0; j < children.getLength(); j++) {
                    org.w3c.dom.Node c = children.item(j);
                    if ("corp_code".equals(c.getNodeName())) {
                        corpCode = c.getTextContent();
                    } else if ("corp_name".equals(c.getNodeName())) {
                        corpName = c.getTextContent();
                    }
                }
                if (corpCode != null && corpName != null) {
                    nameToCorpCode.put(corpName.trim(), corpCode.trim());
                }
            }

            loaded = true;
        } catch (Exception e) {
            // 실패해도 다음에 다시 시도
            e.printStackTrace();
        }
    }

    public String findCorpCodeByName(String companyName) {
        loadIfNeeded();
        // 정확 매칭
        String code = nameToCorpCode.get(companyName);
        if (code != null) return code;

        // 일부만 일치하는 경우 (예: 삼성전자(주))
        for (Map.Entry<String, String> e : nameToCorpCode.entrySet()) {
            if (e.getKey().contains(companyName)) {
                return e.getValue();
            }
        }
        return null;
    }
}
