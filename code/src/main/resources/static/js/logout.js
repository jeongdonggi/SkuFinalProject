// logout.js 파일 내용
$(document).ready(function() {
    $("#logoutButton").click(function() {
        $.ajax({
            url: '/signout',
            method: 'POST',
            success: function(response) {
                alert("로그아웃 성공");
                location.href = "/";
            },
            error: function(error) {
                alert("로그아웃 실패: " + error.responseText);
            }
        });
    });
});
