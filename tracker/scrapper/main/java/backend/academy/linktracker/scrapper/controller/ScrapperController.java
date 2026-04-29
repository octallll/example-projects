package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.api.DefaultApi;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ScrapperController implements DefaultApi {

    private final LinkRepository linkRepository;
    private final LinkService linkService;

    @Override
    public ResponseEntity<Void> tgChatIdPost(Long id) {
        linkRepository.addChat(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> tgChatIdDelete(Long id) {
        if (!linkRepository.existsChat(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        linkRepository.removeChat(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<LinkResponse> linksPost(Long tgChatId, AddLinkRequest addLinkRequest) {
        if (!linkRepository.existsChat(tgChatId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        linkService.addLink(tgChatId, addLinkRequest.getLink(), addLinkRequest.getTags());

        return ResponseEntity.ok(
                new LinkResponse().url(addLinkRequest.getLink()).tags(addLinkRequest.getTags()));
    }

    @Override
    public ResponseEntity<ListLinksResponse> linksGet(Long tgChatId) {
        if (!linkRepository.existsChat(tgChatId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(linkService.getByTgChatId(tgChatId));
    }

    @Override
    public ResponseEntity<LinkResponse> linksDelete(Long tgChatId, RemoveLinkRequest removeLinkRequest) {
        if (!linkRepository.existsChat(tgChatId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        linkService.removeLink(tgChatId, removeLinkRequest.getLink());
        return ResponseEntity.ok(new LinkResponse().url(removeLinkRequest.getLink()));
    }
}
