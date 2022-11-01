// import { useState } from 'react';
// import ShareModal from '../ShareModal';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import WriterProfile from '../WriterProfile';
import { Button, Buttons, Container, EditLink } from './style';

function ContentInfo({ time, user, writerType, questionId, answerId }) {
  // const [shareOpen, setShareOpen] = useState(false);

  // const openModalHandler = () => {
  //   setShareOpen(!shareOpen);
  // };
  const navigate = useNavigate();
  console.log(questionId, answerId);

  // Delete 누르면 axios로 delete 요청. answerId가 있으면 답변 삭제, 없으면 질문 삭제
  const handleDelete = () => {
    if (window.confirm('Delete this page?')) {
      let uri, to;
      if (answerId) {
        uri = `/api/questions/${questionId}/answers/${answerId}`;
        to = `/questions/${questionId}`;
      } else {
        uri = `/api/questions/${questionId}`;
        to = `/`;
      }
      axios
        .delete(uri, {
          headers: {
            Authorization: localStorage.getItem('accesstoken'),
          },
        })
        .then((res) => {
          console.log(res);
          navigate(to);
        })
        .catch((error) => console.log(error));
      navigate(to);
    }
  };

  // displayName 비교해서 본인 글인지 확인

  return (
    <Container>
      <Buttons>
        {/* <Button onClick={openModalHandler}>Share</Button> */}
        <Button>Share</Button>
        {
          <Button>
            <EditLink
              to={`/questions/edit/${questionId}${
                answerId ? '/' + answerId : ''
              }`}
            >
              Edit
            </EditLink>
          </Button>
        }
        {<Button onClick={handleDelete}>Delete</Button>}
        <Button>Follow</Button>
        {/* {shareOpen && (
          <ModalBackdrop onClick={openModalHandler}>
            <StopProp onClick={(e) => e.stopPropagation()}>
              <ShareModal openModalHandler={openModalHandler} />
            </StopProp>
          </ModalBackdrop>
        )} */}
      </Buttons>
      <WriterProfile time={time} user={user} writerType={writerType} />
    </Container>
  );
}

export default ContentInfo;
