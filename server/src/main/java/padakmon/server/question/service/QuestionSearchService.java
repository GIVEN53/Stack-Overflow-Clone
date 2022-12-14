package padakmon.server.question.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import padakmon.server.exception.BusinessLogicException;
import padakmon.server.exception.ExceptionCode;
import padakmon.server.authority.utils.LoggedInUserInfoUtils;
import padakmon.server.question.dto.QuestionSearchDto;
import padakmon.server.question.entity.Question;
import padakmon.server.question.repository.QuestionRepository;
import padakmon.server.tag.repository.TagRepository;

@Service
@Transactional
@AllArgsConstructor
public class QuestionSearchService {

    private final QuestionRepository questionRepository;
    private final LoggedInUserInfoUtils userInfoUtils;
    private final TagRepository tagRepository;

    public Page<Question> findQuestions(int page, int size, Sort sort) {
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return questionRepository.findAll(pageRequest);
    }

    public void setSearchInfo(String query, QuestionSearchDto.SearchInfo searchInfo) {
        String trimmedQuery = query.trim();
        //ํ๊ทธ ๊ฒ์
        if(trimmedQuery.startsWith("tag:")) {
            trimmedQuery = query.substring(4);
            String tagDescription = tagRepository.getDescription(trimmedQuery);

            searchInfo.setSearchTitle("Questions tagged [" + trimmedQuery + "]");
            searchInfo.setTagDescription(tagDescription);
        }
    }

    public String getOrderMode(String orderParam) {
        if(orderParam == null) {
            return Order.NEWEST.orderMode;
        }

        switch(orderParam) {
            case "score":
                return Order.SCORE.orderMode;

            case "newest":
                return Order.NEWEST.orderMode;

            case "name":
                return Order.NAME.orderMode;

            case "popular":
                return Order.POPULAR.orderMode;

            case "reputation":
                return Order.REPUTATION.orderMode;

            case "voters":
                return Order.VOTERS.orderMode;
        }
        throw new BusinessLogicException(ExceptionCode.NOT_VALID_ORDER);
    }

    public Page<Question> delegateSearch(String query, int page, int size, Sort sort) {
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        String trimmedQuery = query.trim();
        //ํ๊ทธ ๊ฒ์
        if(trimmedQuery.startsWith("tag:")) {
            return tagSearch(trimmedQuery, pageRequest);
        }
        //์?์? ๊ฒ์
        //user:mine or user:me (or any user id) returns only your posts (or only the posts from whichever user whose id you entered)
        if(trimmedQuery.startsWith("user:")) {
            return userSearch(trimmedQuery, pageRequest);
        }
        //๋ต๋ณ์ด ๋ช ๊ฐ ๋ฌ๋?ธ๋์ง์ ๋ฐ๋ฅธ ๊ฒ์(0 =>  ์๋ฌด๊ฒ๋ ๋ฌ๋ฆฌ์ง ์์, > 0์ด๋ฉด ์๋?ฅ ๊ฐ๊ณผ ๊ฐ๊ฑฐ๋ ๋ณด๋ค ํฐ)
        if(trimmedQuery.startsWith("answers:")) {
            return answerSearch(trimmedQuery, pageRequest);
        }
        //์ค์ฝ์ด์ ๊ฐ๊ฑฐ๋ ํฐ
        if(trimmedQuery.startsWith("score:")) {
            return scoreSearch(trimmedQuery, pageRequest);
        }
        //์๋ฌด๊ฒ์๋ ํด๋นํ์ง ์์ผ๋ฉด ์ผ๋ฐ ๊ฒ์
        return questionRepository.search(trimmedQuery, pageRequest);
    }

    private Page<Question> tagSearch(String query, PageRequest pageRequest) {
        return questionRepository.tagSearch(query.substring(4), pageRequest);
    }

    private Page<Question> userSearch(String query, PageRequest pageRequest) {
        String userQuery = query.substring(5);
        //๋์ ๊ฒ์ ๊ฒฐ๊ณผ ์ถ๋?ฅ

        //mine์ด๋ me์ธ ๊ฒฝ์ฐ, ๋ก๊ทธ์ธ๋ ์?๋ณด๋ก ์์ด๋ ์ป์ด์ค๊ธฐ
        Long userId = 0L;
        if(userQuery.equals("mine") || userQuery.equals("me")) {
            try {
                userId = userInfoUtils.extractUserId();
            } catch (BusinessLogicException e) {
                userId = 0L;
            }
        //๊ทธ๊ฒ ์๋ ๊ฒฝ์ฐ, ์๋?ฅ๋ ์ซ์๋ก ๊ฒ์
        } else {
            //์ฟผ๋ฆฌ ๊ฒ์์ ์ซ์๋ก ๋ณํ
            try {
                userId = Long.parseLong(userQuery);
            } catch (NumberFormatException e) {
                //์ซ์๋ก ๋ณํ ์๋? ๊ฒฝ์ฐ, ์ผ๋ฐ ๊ฒ์์ผ๋ก
                return questionRepository.search(query, pageRequest);
            }
        }
        return questionRepository.userSearch(userId, pageRequest);
    }

    private Page<Question> answerSearch(String query, PageRequest pageRequest) {
        //๋ท๋ฐ๋ผ ์ค๋ ์ซ์ ํ์ฑ
        int num = 0;
        try {
            num = Integer.parseInt(query.substring(8));
        } catch (NumberFormatException e) {
            //์ซ์๋ก ํ์ฑ ์๋๋ฉด ์ผ๋ฐ ๊ฒ์
            return questionRepository.search(query, pageRequest);
        }
        //0์ด๋ฉด ํ๋๋ ๋ต๋ณ์ด ๋ฌ๋ฆฌ์ง ์์ ๊ฒ์๊ธ
        if(num == 0) {
            return questionRepository.unansweredSearch(pageRequest);
        //0๋ณด๋ค ํฌ๋ฉด ์๋?ฅ๋ ์ซ์์ ๊ฐ๊ฑฐ๋ ๋ณด๋ค ๋ง์ ๋ต๋ณ์ด ๋ฌ๋ฆฐ ๊ฒ์๊ธ
        } else if (num > 0){
            return questionRepository.answeredNumberSearch(num, pageRequest);
        //์์๋ฉด ์ผ๋ฐ ๊ฒ์
        } else {
            return questionRepository.search(query, pageRequest);
        }
    }

    private Page<Question> scoreSearch(String query, PageRequest pageRequest) {
        //๋ท๋ฐ๋ผ ์ค๋ ์ซ์ ํ์ฑ
        int num = 0;
        try {
            num = Integer.parseInt(query.substring(6));
        } catch (NumberFormatException e) {
            //์ซ์๋ก ํ์ฑ ์๋๋ฉด ์ผ๋ฐ ๊ฒ์
            return questionRepository.search(query, pageRequest);
        }

        return questionRepository.scoreSearch(num, pageRequest);
    }
}
