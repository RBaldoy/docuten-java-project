package com.docuten.project.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.docuten.project.model.Document;
import com.docuten.project.model.Signatory;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.DocumentDTO;
import com.docuten.project.model.dto.SignatoryDTO;
import com.docuten.project.model.dto.SignatureProcessDTO;
import com.docuten.project.model.enums.ProcessTypeEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;

@ExtendWith(MockitoExtension.class)
class SignatureProcessMapperTest {

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private SignatoryMapper signatoryMapper;

    @InjectMocks
    private SignatureProcessMapper mapper;

    @Test
    void toDTOMapsScalarFieldsAndChildrenWithoutRepeatingParent() {
        LocalDateTime creationDate = LocalDateTime.now().minusDays(1);
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(2);
        LocalDateTime statusUpdateDate = LocalDateTime.now();
        SignatureProcess process = process(creationDate, expirationDate, statusUpdateDate);
        Document document = new Document();
        Signatory signatory = new Signatory();
        process.setDocument(document);
        process.setSignatories(List.of(signatory));
        DocumentDTO documentDTO = DocumentDTO.builder().fileName("contract.pdf").build();
        SignatoryDTO signatoryDTO = SignatoryDTO.builder().email("ada@example.com").build();
        when(documentMapper.toDTOWithoutSignatureProcess(document)).thenReturn(documentDTO);
        when(signatoryMapper.toDTOWithoutSignatureProcess(signatory)).thenReturn(signatoryDTO);

        SignatureProcessDTO result = mapper.toDTO(process);

        assertEquals(process.getId(), result.getId());
        assertEquals(process.getProcessType(), result.getProcessType());
        assertEquals(process.getStatus(), result.getStatus());
        assertEquals(creationDate, result.getCreationDate());
        assertEquals(expirationDate, result.getExpirationDate());
        assertEquals(statusUpdateDate, result.getStatusUpdateDate());
        assertSame(documentDTO, result.getDocument());
        assertEquals(List.of(signatoryDTO), result.getSignatories());
        verify(documentMapper).toDTOWithoutSignatureProcess(document);
        verify(signatoryMapper).toDTOWithoutSignatureProcess(signatory);
    }

    @Test
    void toEntityMapsChildrenAndRestoresTheirParentReference() {
        DocumentDTO documentDTO = DocumentDTO.builder().fileName("contract.pdf").build();
        SignatoryDTO signatoryDTO = SignatoryDTO.builder().email("ada@example.com").build();
        SignatureProcessDTO dto = SignatureProcessDTO.builder()
                .processType(ProcessTypeEnum.PARALLEL)
                .status(SignatureProcessStatusEnum.PENDING)
                .creationDate(LocalDateTime.now().minusDays(1))
                .expirationDate(LocalDateTime.now().plusDays(2))
                .statusUpdateDate(LocalDateTime.now())
                .document(documentDTO)
                .signatories(List.of(signatoryDTO))
                .build();
        Document document = new Document();
        Signatory signatory = new Signatory();
        when(documentMapper.toEntityWithoutSignatureProcess(documentDTO)).thenReturn(document);
        when(signatoryMapper.toEntityWithoutSignatureProcess(signatoryDTO)).thenReturn(signatory);

        SignatureProcess result = mapper.toEntity(dto);

        assertEquals(dto.getProcessType(), result.getProcessType());
        assertEquals(dto.getStatus(), result.getStatus());
        assertEquals(dto.getCreationDate(), result.getCreationDate());
        assertEquals(dto.getExpirationDate(), result.getExpirationDate());
        assertEquals(dto.getStatusUpdateDate(), result.getStatusUpdateDate());
        assertSame(document, result.getDocument());
        assertSame(result, document.getSignatureProcess());
        assertEquals(List.of(signatory), result.getSignatories());
        assertSame(result, signatory.getSignatureProcess());
        verify(documentMapper).toEntityWithoutSignatureProcess(documentDTO);
        verify(signatoryMapper).toEntityWithoutSignatureProcess(signatoryDTO);
    }

    @Test
    void toDTOAndToEntityReturnNullForNullInput() {
        assertNull(mapper.toDTO(null));
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toDTOAndToEntityPreserveNullChildCollections() {
        SignatureProcess process = process(null, null, null);
        SignatureProcessDTO dto = mapper.toDTO(process);
        assertNull(dto.getDocument());
        assertNull(dto.getSignatories());

        SignatureProcess entity = mapper.toEntity(SignatureProcessDTO.builder().build());
        assertNull(entity.getDocument());
        assertNull(entity.getSignatories());
    }

    private static SignatureProcess process(LocalDateTime creation, LocalDateTime expiration,
            LocalDateTime statusUpdate) {
        SignatureProcess process = new SignatureProcess();
        process.setId(7L);
        process.setProcessType(ProcessTypeEnum.SEQUENTIAL);
        process.setStatus(SignatureProcessStatusEnum.PENDING);
        process.setCreationDate(creation);
        process.setExpirationDate(expiration);
        process.setStatusUpdateDate(statusUpdate);
        return process;
    }
}
