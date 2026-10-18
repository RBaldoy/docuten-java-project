package com.docuten.project.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.docuten.project.model.Document;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.DocumentDTO;
import com.docuten.project.model.dto.SignatureProcessDTO;
import com.docuten.project.model.enums.ProcessTypeEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;

@ExtendWith(MockitoExtension.class)
class DocumentMapperTest {

    @Mock
    private SignatureProcessMapper signatureProcessMapper;

    @InjectMocks
    private DocumentMapper mapper;

    @Test
    void toDTOMapsAllFieldsAndSignatureProcess() {
        LocalDateTime creationDate = LocalDateTime.now();
        SignatureProcess process = new SignatureProcess();
        Document document = document(creationDate, process);
        SignatureProcessDTO processDTO = SignatureProcessDTO.builder().id(15L).build();
        when(signatureProcessMapper.toDTO(process)).thenReturn(processDTO);

        DocumentDTO result = mapper.toDTO(document);

        assertEquals(document.getId(), result.getId());
        assertEquals(document.getFileName(), result.getFileName());
        assertEquals(document.getStoragePath(), result.getStoragePath());
        assertEquals(document.getHash(), result.getHash());
        assertEquals(document.getSize(), result.getSize());
        assertEquals(creationDate, result.getCreationDate());
        assertSame(processDTO, result.getSignatureProcess());
        verify(signatureProcessMapper).toDTO(process);
    }

    @Test
    void toEntityMapsFieldsAndSignatureProcess() {
        SignatureProcessDTO processDTO = SignatureProcessDTO.builder()
                .processType(ProcessTypeEnum.PARALLEL)
                .status(SignatureProcessStatusEnum.PENDING)
                .build();
        DocumentDTO dto = DocumentDTO.builder()
                .id(8L)
                .fileName("contract.pdf")
                .storagePath("storage/contract.pdf")
                .hash("hash")
                .size(128)
                .creationDate(LocalDateTime.now())
                .signatureProcess(processDTO)
                .build();
        SignatureProcess process = new SignatureProcess();
        when(signatureProcessMapper.toEntity(processDTO)).thenReturn(process);

        Document result = mapper.toEntity(dto);

        assertEquals(dto.getFileName(), result.getFileName());
        assertEquals(dto.getStoragePath(), result.getStoragePath());
        assertEquals(dto.getHash(), result.getHash());
        assertEquals(dto.getSize(), result.getSize());
        assertEquals(dto.getCreationDate(), result.getCreationDate());
        assertSame(process, result.getSignatureProcess());
        verify(signatureProcessMapper).toEntity(processDTO);
    }

    @Test
    void toDTOAndToEntityReturnNullForNullInput() {
        assertNull(mapper.toDTO(null));
        assertNull(mapper.toEntity(null));
    }

    @Test
    void childMappingOmitsBackReferenceToPreventRecursiveMapping() {
        SignatureProcess process = new SignatureProcess();
        Document document = document(LocalDateTime.now(), process);

        assertNull(mapper.toDTOWithoutSignatureProcess(document).getSignatureProcess());
        assertNull(mapper.toEntityWithoutSignatureProcess(
                DocumentDTO.builder().signatureProcess(SignatureProcessDTO.builder().build()).build())
                .getSignatureProcess());
    }

    private static Document document(LocalDateTime creationDate, SignatureProcess process) {
        Document document = new Document();
        document.setId(4L);
        document.setFileName("contract.pdf");
        document.setStoragePath("storage/contract.pdf");
        document.setHash("hash-value");
        document.setSize(128);
        document.setCreationDate(creationDate);
        document.setSignatureProcess(process);
        return document;
    }
}
