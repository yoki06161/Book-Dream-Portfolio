function sendDataToServer(dataArray, csrfHeader, csrfToken) {
	// 배열을 JSON 문자열로 변환
	let jsonData = JSON.stringify(dataArray);

	fetch('/basket/add', {
		method: 'POST',
	    headers: {
	        'Content-Type': 'application/json',
	        [csrfHeader]: csrfToken
	    },
	    body: jsonData  // jsonData를 전송
	})
	.then(response => {
	      if (!response.ok) {
	          return response.text().then(text => {
	              throw new Error(`Network response was not ok: ${text}`);
	          });
	      }
	      // 응답이 JSON이 아닌 경우를 대비
	      return response.text().then(text => {
	          try {
	              return JSON.parse(text);
	          } catch (error) {
	              console.error('Error parsing JSON:', error);
	              throw new Error('Invalid JSON response');
	          }
	      });
	  })
	.then(data => {
	    console.log('Success:', data);
	})
	.catch(error => {
		if (error.response) {
		    // 서버 응답이 있을 경우
		    console.error('서버 응답 오류:', error.response.data);
		} else if (error.request) {
		    // 요청이 전송되지 않았을 경우
		    console.error('요청이 전송되지 않음:', error.request);
		} else {
		    // 요청 설정 중 오류가 발생했을 경우
		    console.error('요청 설정 중 오류 발생:', error.message);
		}
	});
}

function init() {
	// 세션 스토리지 전체 삭제(저장이 잘못되었을때 주석풀고 사용)
	//sessionStorage.clear();
	
	// 세션 스토리지에서 장바구니 내역 배열을 불러오기 (없으면 빈 배열로 초기화)
	let dataArray = JSON.parse(sessionStorage.getItem("dataArray")) || [];

	// 장바구니 배열 길이 가져오기
	let badgeCount = dataArray.length;

	// sessionStorage에 badgeCount 저장
	sessionStorage.setItem('badgeCount', badgeCount);

	// 뱃지 숫자 설정
	document.getElementById('badge').textContent = badgeCount;
}

// DOMContentLoaded 이벤트 리스너를 사용하여 문서가 완전히 로드된 후 실행하는 함수 호출
document.addEventListener('DOMContentLoaded', init);

// 장바구니 추가 함수
function aa() {
	let book_id = parseInt(document.querySelector('.book_id').value);
	// 모든 이미지를 선택하여 배열로 변환
	let images = document.querySelectorAll('img');

	// 두 번째 이미지의 src를 가져오기
	let book_img = images[1].src;

	let book_title = document.querySelector('.title').textContent;
	let book_writer = document.querySelector('.writer').textContent;
	let book_price = document.querySelector('.count').textContent; 
	let count = parseInt(document.querySelector('.bcount').value);
	let count_price = document.querySelector('.result').textContent + '원';

	let dataToStore = {
		book_id: book_id,
	    book_img: book_img,
	    book_title: book_title,
	    book_writer: book_writer,
	    book_price: book_price,
	    count: count,
	    count_price: count_price
	};

	let dataArray = JSON.parse(sessionStorage.getItem("dataArray")) || [];

	if (!dataArray.some(item => item.book_id === book_id)) {
		dataArray.push(dataToStore);
	    sessionStorage.setItem("dataArray", JSON.stringify(dataArray));
	    alert('장바구니에 추가되었습니다.');

	    var badge = parseInt(document.getElementById('badge').textContent);
	    var newBadgeCount = badge + 1;
	    document.getElementById('badge').textContent = newBadgeCount;
	    sessionStorage.setItem('badgeCount', newBadgeCount);

		// 로그인했다면 장바구니 DB에 추가
		if (isAuthenticated) {
			sendDataToServer(dataArray, csrfHeader, csrfToken);
		}
	} else {
	    alert('이미 장바구니에 있습니다.');
	    console.log(dataArray);
	}
}

// 'buy' 클래스를 가진 모든 요소를 가져옵니다
var buyButtons = document.getElementsByClassName('buy');

// '바로구매' 버튼에 클릭 이벤트 리스너를 추가
for (var i = 0; i < buyButtons.length; i++) {
    buyButtons[i].addEventListener('click', function() {
		let book_id = parseInt(document.querySelector('.book_id').value);
		// 모든 이미지를 선택하여 배열로 변환
		let images = document.querySelectorAll('img');

		// 두 번째 이미지의 src를 가져오기
		let book_img = images[1].src;
		let book_title = document.querySelector('.title').textContent;
		let book_writer = document.querySelector('.writer').textContent;
		let book_price = document.querySelector('.count').textContent;
		let count = parseInt(document.querySelector('.bcount').value);
		let count_price = document.querySelector('.result').textContent + '원';
		// count_price를 totalSum(숫자 값)으로 저장
		let totalSum = parseFloat(count_price.replace(/[^\d.-]/g, ''));
		
		let selectedItem = {
			book_id: book_id,
		    book_img: book_img,
		    book_title: book_title,
		    book_writer: book_writer,
		    book_price: book_price,
		    count: count,
		    count_price: count_price
		};
		
    	let selectedItems = [];

		selectedItems.push(selectedItem);
		sessionStorage.setItem("selectedItems", JSON.stringify(selectedItems));
		sessionStorage.setItem('totalSum', totalSum); 
		location.href='/order';
    });
}
