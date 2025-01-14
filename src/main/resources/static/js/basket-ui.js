// 전역 변수 선언
let totalSum = 0; // 총 가격 합계 변수 선언

// 주문하기 버튼 상태 변경(전역 함수)
function updateButtonState() {
    const checkboxes = document.querySelectorAll('input[type="checkbox"]:not(#select_all)');
    const anyChecked = Array.from(checkboxes).some(checkbox => checkbox.checked);
    document.getElementById('order').disabled = !anyChecked;
}

function updateTotalSum() {
    totalSum = 0;

    const checkboxes = document.querySelectorAll('input[type="checkbox"]:not(#select_all)');
    checkboxes.forEach(function(checkbox, index) {
        if (checkbox.checked) {
            let price = parseFloat(document.querySelector(`.row${index} .result`).textContent.replace(/[^0-9]/g, ''));
            totalSum += price;
        }
    });

    let totalSumElement = document.getElementById('totalSum');
    if (totalSumElement) {
        totalSumElement.textContent = totalSum.toLocaleString() + '원';
    }
}

function init() {
	const selectAllCheckbox = document.getElementById('select_all'); // 전체 선택 체크박스 요소
	const orderButton = document.getElementById('order'); // 주문 버튼 요소

	// 주문 버튼 상태 업데이트 함수
	function updateButtonState() {
	    const checkboxes = document.querySelectorAll('input[type="checkbox"]:not(#select_all)');
	    const anyChecked = Array.from(checkboxes).some(checkbox => checkbox.checked);
	    orderButton.disabled = !anyChecked;
	}
	
    // 전체 선택 체크박스 상태 업데이트 함수
    function updateSelectAllState() {
        const checkboxes = document.querySelectorAll('input[type="checkbox"]:not(#select_all)');
        const allChecked = Array.from(checkboxes).every(checkbox => checkbox.checked);
        selectAllCheckbox.checked = allChecked;
    }

    // 체크박스 변경 이벤트 핸들러
    function handleCheckboxChange() {
        updateButtonState();
        updateSelectAllState();
        updateTotalSum();
    }

    // 전체 선택 체크박스 클릭 이벤트 리스너
    selectAllCheckbox.addEventListener('change', function () {
        const checkboxes = document.querySelectorAll('input[type="checkbox"]:not(#select_all)');
        const selectAll = selectAllCheckbox.checked;
        checkboxes.forEach((checkbox) => {
            checkbox.checked = selectAll;
        });
        updateButtonState();
        updateTotalSum();
    });

    // 문서의 체크박스 변경 이벤트 리스너
    document.addEventListener('change', function (event) {
        if (event.target.matches('input[type="checkbox"]:not(#select_all)')) {
            handleCheckboxChange();
        }
    });

    displayFormData(); // 데이터 표시 함수 호출
}

// 데이터 가져오기 함수
function retrieveData(index) {
    const row = document.querySelector(`.row${index}`);
    const bookImage = row.querySelector('img').src; // 행 내의 이미지를 선택
    const bookId = row.querySelector('input[type="hidden"]').value;
    const bookTitle = row.querySelector('.fs-5.fw-bold').textContent;
    const bookWriter = row.querySelector('.fs-6').textContent;
    const bookPrice = row.querySelector('.price').textContent;
    const count = row.querySelector('select').value;
    const countPrice = row.querySelector('.result').textContent;

    return {
        book_id: bookId,
        book_img: bookImage,
        book_title: bookTitle,
        book_writer: bookWriter,
        book_price: bookPrice,
        count: count,
        count_price: countPrice
    };
}

// 데이터 표시 함수
function displayFormData() {
    let dataArray = JSON.parse(sessionStorage.getItem("dataArray")) || []; // 세션 스토리지에서 데이터 배열 가져오기
    let dataArrayList = document.getElementById('dataArrayList'); // 상품 목록 테이블 요소
    let dataNotFound = document.getElementById('dataNotFound'); // 상품이 없을 때 표시할 요소

    // 기존 상품 목록 초기화
    dataArrayList.innerHTML = '';
    dataNotFound.innerHTML = ''; // 메시지 영역 초기화

    if (dataArray.length > 0) {
        // 데이터 배열을 순회하며 각 상품 정보를 테이블에 추가
        dataArray.forEach(function(data, index) {
            let tr = document.createElement('tr'); // 새로운 행 요소 생성

            // 수량 선택 셀 추가
            let selectElement = document.createElement('select');
            selectElement.className = `count${index}`;
            selectElement.name = `count`;
            for (let i = 1; i <= 10; i++) {
                let option = document.createElement('option');
                option.value = i;
                option.textContent = i;
                if (i == data.count) {
                    option.selected = true;
                }
                selectElement.appendChild(option);
            }

            // 수량 변경 이벤트 리스너 추가
            selectElement.addEventListener('change', function() {
                updateCountPrice(index); // 수량 변경 시 수량당 가격 업데이트
            });

            // 삭제 버튼 추가
            let btnClose = document.createElement('button');
            btnClose.type = 'button';
            btnClose.className = 'btn-close';
            btnClose.setAttribute('aria-label', 'Close');
            btnClose.setAttribute('data-bs-toggle', 'modal');
            btnClose.setAttribute('data-bs-target', `#deleteModal${index}`);

            // 행 내용 설정
            tr.innerHTML = `
                <input type="hidden" value=${data.book_id} name="book_id">
                <td><input class="form-check-input" type="checkbox"></td>
                <td><img src=${data.book_img} alt="상품사진" style="width: 82px; height: 118.34px;"></td>
                <td class="text-start"><p class="fs-5 fw-bold">${data.book_title}</p>
                <p class="fs-6" style="color:gray;">${data.book_writer}</p>
                <p class="fs-5 price">${data.book_price}</p></td>
                <td></td>
                <td class="result" name="count_price">${data.count_price}</td>
                <td></td>`;

            // 수량 선택 셀과 단위 추가
            tr.children[4].appendChild(selectElement);
            tr.children[4].appendChild(document.createTextNode(' 권'));

            // 삭제 버튼 셀 추가
            let tdClose = document.createElement('td');
            tdClose.appendChild(btnClose);
            tr.appendChild(tdClose);

            // 행 클래스 설정
            tr.className = `row${index}`;
            dataArrayList.appendChild(tr); // 테이블에 행 추가

            // 삭제 모달 추가
            let modal = document.createElement('div');
            modal.className = 'modal fade';
            modal.id = `deleteModal${index}`;
            modal.tabIndex = -1;
            modal.setAttribute('data-bs-backdrop', 'static');
            modal.innerHTML = `
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h1 class="modal-title fs-5">장바구니 상품 삭제</h1>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            장바구니에 담긴 이 상품을 삭제하시겠어요?
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary delete" data-index="${index}">예</button>
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style="border: 0px;">아니오(삭제 취소)</button>
                        </div>
                    </div>
                </div>
            `;

            document.body.appendChild(modal); // 삭제 모달을 문서에 추가
        });

        initCheckboxes(); // 체크박스 초기화 함수 호출
    } else {
        // 상품이 없을 때 메시지 표시
        let div = document.createElement('div');
        div.innerHTML = `<h4 class="text-center">장바구니에 담은 상품이 없습니다</h4>`;
        dataNotFound.appendChild(div); // 메시지를 표시할 요소에 추가
        // 장바구니가 비어 있을 경우 전체 선택 체크박스 해제
        document.getElementById('select_all').checked = false;
    }
}

// 수량당 가격 업데이트 함수
function updateCountPrice(index) {
	let countSelect = document.querySelector(`.row${index} select`); // 선택된 수량 select 엘리먼트
	if (!countSelect) return; // 선택된 엘리먼트가 없으면 함수 종료

	let count = parseInt(countSelect.value); // 선택된 수량
	let priceText = document.querySelector(`.row${index} .price`).textContent; // 상품 가격 텍스트
	let price = parseFloat(priceText.replace(/[^0-9]/g, '')); // 가격에서 숫자 부분 추출

	// 수량당 가격 업데이트
	let resultElement = document.querySelector(`.row${index} .result`);
	if (resultElement) {
	    let countPrice = (count * price).toLocaleString(); // 수량당 가격 계산 후 통화 형식으로 변환
	    resultElement.textContent = countPrice + '원'; // 결과 엘리먼트에 텍스트 설정
	}

	updateTotalSum(); // 이 부분에서 updateTotalSum 함수 호출
}

// 삭제 버튼 클릭 이벤트 리스너
document.addEventListener('click', function(event) {
    if (event.target.classList.contains('delete')) {
        event.stopPropagation();
        let index = event.target.getAttribute('data-index');
        let dataArray = JSON.parse(sessionStorage.getItem("dataArray")) || [];
        deleteItem(index, dataArray[index]);

        // 모달 닫기
        let modal = bootstrap.Modal.getInstance(document.getElementById(`deleteModal${index}`));
        if (modal) {
            modal.hide();
        }
    }
});

// 아이템 삭제 함수
function deleteItem(index, data) {
    let bookIdToDelete = data.book_id; // 삭제할 상품 ID
    let dataArray = JSON.parse(sessionStorage.getItem("dataArray")) || []; // 세션 스토리지에서 데이터 배열 가져오기
    let indexToDelete = dataArray.findIndex(item => item.book_id === bookIdToDelete); // 삭제할 상품의 인덱스 찾기

    if (indexToDelete !== -1) {
        dataArray.splice(indexToDelete, 1); // 배열에서 상품 삭제
        sessionStorage.setItem("dataArray", JSON.stringify(dataArray)); // 세션 스토리지에 업데이트된 배열 저장
        let badgeCount = dataArray.length; // 장바구니 아이템 개수 업데이트
        sessionStorage.setItem('badgeCount', badgeCount); // 세션 스토리지에 업데이트된 아이템 개수 저장
        document.getElementById('badge').textContent = badgeCount; // UI에 아이템 개수 업데이트

		// 로그인했다면 장바구니 DB에서 삭제 
		if (typeof isAuthenticated !== 'undefined' && isAuthenticated) {
			sendDataToServerDelete(bookIdToDelete, csrfHeader, csrfToken);
		}
		
        displayFormData(); // 데이터를 삭제한 후 UI 다시 렌더링
    }
}

function sendDataToServerDelete(bookIdToDelete, csrfHeader, csrfToken) {
    fetch('/basket/delete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({ book_id: bookIdToDelete })  
		// bookIdToDelete를 JSON으로 변환하여 전송  // jsonData를 전송
    })
	.then(response => {
	     if (!response.ok) {
	         return response.json().then(errData => {
	             throw new Error('서버 응답 오류: ' + JSON.stringify(errData));
	         });
	     }
	     return response.json();
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

// 체크박스 초기화 함수
function initCheckboxes() {
    const checkboxes = document.querySelectorAll('input[type="checkbox"]:not(#select_all)');
    checkboxes.forEach(function(checkbox) {
        checkbox.checked = false; // 모든 체크박스 초기화
    });

    updateButtonState(); // 버튼 상태 업데이트
    updateTotalSum(); // 총 가격 업데이트
}

// 주문 버튼 클릭 이벤트 리스너
document.getElementById('order').addEventListener('click', function() {
    let selectedItems = [];
    let checkboxes = document.querySelectorAll('input[type="checkbox"]:not(#select_all)'); // 체크박스들
    let totalSum = parseFloat(document.getElementById('totalSum').textContent.replace(/[^\d.-]/g, '')); // 총 가격 값

    checkboxes.forEach(function(checkbox, index) {
        if (checkbox.checked) {
            let data = retrieveData(index); // 체크된 항목의 데이터를 가져옴 (index에 해당하는 데이터를 가져오는 함수 필요)
            selectedItems.push(data);
        }
    });

    // 선택된 항목들과 총가격을 세션 스토리지에 저장
    sessionStorage.setItem('selectedItems', JSON.stringify(selectedItems));
    sessionStorage.setItem('totalSum', totalSum); // totalSum을 숫자 값으로 저장
});

// 초기화 함수
document.addEventListener('DOMContentLoaded', init);