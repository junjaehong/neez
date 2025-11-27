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

        MeetRTChunk chunk = MeetRTChunk.builder()
                .meetIdx(meetIdx)
                .seq(seq)
                .langCode(langCode)
                .content(content)
                .chunkType("STT")
                .finalChunk(isFinal)
                .build();

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
