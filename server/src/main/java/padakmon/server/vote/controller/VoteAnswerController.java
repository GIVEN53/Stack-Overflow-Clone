package padakmon.server.vote.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import padakmon.server.vote.dto.VoteDto;
import padakmon.server.vote.service.VoteService;

import javax.validation.Valid;

@RestController
@RequestMapping("/questions/{question-id}/answers/{answer-id}/votes")
public class VoteAnswerController {
    private VoteService voteService;
    @PostMapping
    public ResponseEntity vote(@PathVariable(name = "answer-id") long answerId, @RequestBody @Valid VoteDto.Post post) {
        VoteDto.Response response;
        if(post.getVote() == 1) {
            response = voteService.answerCountUp(answerId);
        } else {
            response = voteService.answerCountDown(answerId);
        }
        return new ResponseEntity(response, HttpStatus.OK);
    }
}
