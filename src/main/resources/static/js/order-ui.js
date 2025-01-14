function init() {
    // list 페이지에서 체크된 항목들의 데이터가 담긴 배열+총가격 가져오기
    let selectedItems = JSON.parse(sessionStorage.getItem('selectedItems')) || [];
    let totalSum = parseFloat(sessionStorage.getItem('totalSum')) || 0;

    let totalSumElement = document.getElementById('totalSum');
    if (totalSumElement) {
        totalSumElement.textContent = totalSum.toLocaleString() + '원';
    }

    const form = document.getElementById('orderForm');
    const submitBtn = document.getElementById('pay');
    const inputs = form.querySelectorAll('input[type="text"], input[type="number"], input[type="password"]');
    const pwError = document.getElementById('pwError');
    const pwFormatError = document.getElementById('pwFormatError');
    //const customInput = document.getElementById('customInput');

    // 필수 입력 필드 검사
    const checkInputs = () => {
        let allFilled = true;
        inputs.forEach((input) => {
            if (input.name !== 'customInput' && input.value.trim() === '') {
                allFilled = false;
            }
        });
        return allFilled;
    };

    // 비밀번호 유효성 검사
    const checkPasswords = () => {
        const pw = document.getElementById('pw').value;
        const pw2 = document.getElementById('pw2').value;

        const pwValid = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/.test(pw);

        if (!pwValid) {
            pwError.style.display = 'none';
            pwFormatError.style.display = 'block';
            return false;
        } else if (pw !== pw2) {
            pwError.style.display = 'block';
            pwFormatError.style.display = 'none';
            return false;
        } else {
            pwError.style.display = 'none';
            pwFormatError.style.display = 'none';
            return true;
        }
    };

    // 전체 폼 유효성 검사
    const validateForm = () => {
        const allInputsFilled = checkInputs();
        const passwordsValid = checkPasswords();
        submitBtn.disabled = !(allInputsFilled && passwordsValid);
    };

    inputs.forEach((input) => {
        input.addEventListener('input', validateForm);
        // 추가: input[type="number"]에 대한 maxlength 처리
        if (input.type === 'number' && input.name === 'phone') {
            input.addEventListener('input', (event) => {
                if (input.value.length > 11) {
                    input.value = input.value.slice(0, 11);
                }
            });
        }
    });

   // document.getElementById('options').addEventListener('change', () => {
  //      const selectBox = document.getElementById('options');
  //      if (selectBox.value === 'directInput') {
  //          customInput.style.display = 'block';
  //      } else {
   //         customInput.style.display = 'none';
   //     }
  //      validateForm();
  //  });

    validateForm();

    displayFormData();
}

// 우편번호+주소 입력 API
function sample6_execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function (data) {
            var addr = ''; 
            var extraAddr = '';

            if (data.userSelectedType === 'R') {
                addr = data.roadAddress;
            } else {
                addr = data.jibunAddress;
            }

            if (data.userSelectedType === 'R') {
                if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
                    extraAddr += data.bname;
                }
                if (data.buildingName !== '' && data.apartment === 'Y') {
                    extraAddr +=
                        extraAddr !== '' ? ', ' + data.buildingName : data.buildingName;
                }
                if (extraAddr !== '') {
                    extraAddr = ' (' + extraAddr + ')';
                }
            } else {
                extraAddr = '';
            }

            document.getElementById('sample6_postcode').value = data.zonecode;
            document.getElementById('sample6_address').value = addr + extraAddr;
            document.getElementById('sample6_detailAddress').focus();
        },
    }).open();
}

// 폼 데이터 출력
function displayFormData() {
    let selectedItems = JSON.parse(sessionStorage.getItem('selectedItems')) || [];
    let dataArrayList = document.getElementById('dataArrayList');

    dataArrayList.innerHTML = '';

    if (selectedItems.length > 0) {
        selectedItems.forEach(function(data, index) {
            let tr = document.createElement('tr');

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

            tr.innerHTML = `
                <input type="hidden" value=${data.book_id} name="book_id">
                <td><img src=${data.book_img} alt="상품사진" style="width: 82px; height: 118.34px;"></td>
                <td class="text-start"><p class="fs-5 fw-bold">${data.book_title}</p>
                    <p class="fs-6" style="color:gray;">${data.book_writer}</p>
                <p class="fs-5 price">${data.book_price}</p></td>
                <td>${data.count}권</td>
                <td class="result" name="count_price">${data.count_price}</td>`;

            dataArrayList.appendChild(tr);
        });
    }
}

// 결제 버튼 클릭 이벤트 리스너
document.getElementById('pay').addEventListener('click', function() {
    // list 페이지에서 체크된 항목들의 데이터가 담긴 배열+총가격 가져오기
    let selectedItems = JSON.parse(sessionStorage.getItem('selectedItems')) || [];
    let totalSum = parseFloat(sessionStorage.getItem('totalSum'));
	// order 페이지에서 데이터 받아오기
	let name = document.getElementById('name').value;
	let phone = document.getElementById('phone').value;
	let pw = document.getElementById('pw').value || 0;
	let post_code = document.getElementById('sample6_postcode').value;
	let address = document.getElementById('sample6_address').value;
	let detail_address = document.getElementById('sample6_detailAddress').value;
	let full_addr = address + " " + detail_address;
	let options = document.getElementById('options').value;
	//let customInput = document.getElementById('customInput').value;
	
    // 결제모듈
    const IMP = window.IMP;
    IMP.init('imp45767108'); /* imp~ : 가맹점 식별코드 */

    // IMP 객체를 사용하여 결제를 요청
    IMP.request_pay({ // 결제 요청
        pg: "nice",   // PG사 설정
        pay_method: "card",   // 결제 방법
        name: "서적", // 상품명
		buyer_name: name,
        //amount: 100,   // 결제 가격 (테스트용)
        amount: totalSum,  // 실제 결제 가격
		buyer_name: name,
		pw: pw,
		buyer_tel: phone,
		buyer_postcode: post_code,
		buyer_addr: full_addr,
        merchant_uid: new Date().getTime(), // 임의 주문번호 부여
		options: options,
		
    }, function(res) {
        if (res.success) {
            axios({
                method: "post",
                url: `/payment/validation/${res.imp_uid}`,
				headers: {
					'Content-Type': 'application/json',
					[csrfHeader]: csrfToken
				}
            })
            .then(response => {
				// 결제성공시 imp_uid, 주문조회pw, 배송요청사항을 post요청해서 pay테이블 update
				axios({
					method: "post",
					url: '/payment/updatePay',
					headers: {
						'Content-Type': 'application/json',
						[csrfHeader]: csrfToken
					},
					data: {
						imp_uid:res.imp_uid,
						pw: pw,
						options: options
					}
				})
				.then(response => {
					// pay테이블 update 성공시, 세션스토리지 내역 업데이트
					// 배열의 각 항목에 pay_id 속성 추가
					selectedItems.forEach(item => {
					    item.pay_id = res.imp_uid; 
					});

					// 변경된 배열을 세션 스토리지에 저장(되는 것 확인함)
					sessionStorage.setItem('selectedItems', JSON.stringify(selectedItems));

					// pay테이블 update 성공시, orders테이블에 세션스토리지 내역 저장
					axios({
						method: "post",
						url: '/order/addProducts',
						headers: {
							'Content-Type': 'application/json',
							[csrfHeader]: csrfToken
						},
						// 배열 selectedItems를 JSON 문자열로 변환
						data: JSON.stringify(selectedItems)
					})
					.then(response => {
						console.log("orders update response:", response.data);

						location.href=`/order/success/${res.imp_uid}`;
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
        } else {
            // 실패 메시지 표시
            let resultMessage = `
                <p>결제에 실패했습니다.</p>
                <p>Error: ${res.error_msg}</p>
            `;
            resultWindow.document.getElementById('resultMessage').innerHTML = resultMessage;
            console.error(res.error_msg);
        }
    });
});

document.addEventListener('DOMContentLoaded', init);