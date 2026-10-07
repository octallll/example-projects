package backend.academy.linktracker.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.config.FilterConfig;
import backend.academy.linktracker.ai.filter.FilterUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class FilterTest extends TestcontainersConfiguration {
    @MockitoBean
    private FilterConfig filterConfig;

    @MockitoBean
    private FilterConfig.SummarizationConfig summarizationConfig;

    @MockitoBean
    private ChatModel chatModel;

    @Autowired
    private FilterUtils filterUtils;

    @BeforeEach
    void setUp() {
        when(filterConfig.getSummarization()).thenReturn(summarizationConfig);
    }

    @Test
    void stopWordsFilterTest() {
        when(filterConfig.getStopWords()).thenReturn(List.of("spam", "promo", "ads"));
        when(filterConfig.getExcludedAuthors()).thenReturn(List.of());
        when(filterConfig.getMinLength()).thenReturn(0);

        assertFalse(filterUtils.validateMessage("This is spam", "author"));
        assertFalse(filterUtils.validateMessage("Promo ads", "spam"));
        assertTrue(filterUtils.validateMessage("The base message", "author"));
    }

    @Test
    void excludedAuthorsTest() {
        when(filterConfig.getStopWords()).thenReturn(List.of());
        when(filterConfig.getExcludedAuthors()).thenReturn(List.of("author1", "killer"));
        when(filterConfig.getMinLength()).thenReturn(0);

        assertFalse(filterUtils.validateMessage("Not a spam", "author1"));
        assertFalse(filterUtils.validateMessage("Hello", "killer"));
        assertTrue(filterUtils.validateMessage("The base message", "normal-author"));
    }

    @Test
    void minimalLengthTest() {
        when(filterConfig.getMinLength()).thenReturn(20);

        assertFalse(filterUtils.validateMessage("Short", "author"));
    }

    @Test
    void testValidMessage() {
        when(filterConfig.getMinLength()).thenReturn(10);
        when(filterConfig.getExcludedAuthors()).thenReturn(List.of("blocked-user"));
        when(filterConfig.getStopWords()).thenReturn(List.of("spam"));

        assertTrue(filterUtils.validateMessage("This is a valid long message", "good-author"));
    }

    @Test
    void shouldSummarizeLongMessage() {
        String longText = "Very long text....".repeat(20);
        String expectedSummary = "This is a short summary.";

        when(summarizationConfig.getThreshold()).thenReturn(100);

        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        DeepSeekAssistantMessage mockOutput = mock(DeepSeekAssistantMessage.class);

        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);
        when(mockResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(mockOutput);
        when(mockOutput.getText()).thenReturn(expectedSummary);

        String result = filterUtils.summarizeMessage(longText);

        assertEquals(expectedSummary, result);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void shouldNotSummarizeShortMessage() {
        String shortText = "Short message";
        when(summarizationConfig.getThreshold()).thenReturn(100);

        String result = filterUtils.summarizeMessage(shortText);

        assertEquals(shortText, result);
        verifyNoInteractions(chatModel);
    }
}
