package com.bbey.neez.service.Meet;

import com.bbey.neez.entity.Meet.MeetRTChunk;
import com.bbey.neez.repository.Meet.MeetRTChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingSttService {

    private final MeetRTChunkRepository chunkRepository;

    /**
     * STT 청크 한 건 저장
     *
     * @param meetIdx  회의 PK
     * @param seq      청크 순번
     * @param langCode 언어 코드(ko, en 등)
     * @param content  인식된 텍스트
     * @param isFinal  final인지 여부
     */
    public MeetRTChunk saveChunk(Long meetIdx,
            Long seq,
            String langCode,
            String content,
            boolean isFinal) {

        MeetRTChunk chunk = new MeetRTChunk();
        chunk.setMeetIdx(meetIdx);
        chunk.setSeq(seq);
        chunk.setChunkType("TRANSCRIPT"); // ★ 여기 중요: TRANSCRIPT 로 고정
        chunk.setLangCode(langCode);
        chunk.setContent(content);
        chunk.setFinalChunk(isFinal); // 필드명이 finalChunk / isFinal 이면 거기에 맞게 수정

        return chunkRepository.save(chunk);
    }

    /**
     * 해당 회의의 모든 청크를 seq 순으로 이어붙여 전체 회의록 텍스트를 만든다.
     */
    @Transactional(readOnly = true)
    public String buildFullTranscript(Long meetIdx) {
        List<MeetRTChunk> chunks = chunkRepository.findByMeetIdxOrderBySeqAsc(meetIdx);

        StringBuilder sb = new StringBuilder();
        for (MeetRTChunk c : chunks) {
            if (sb.length() > 0)
                sb.append("\n");
            sb.append(c.getContent());
        }
        return sb.toString();
    }
}
