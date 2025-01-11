function selectedColor(color) {
    var user = document.getElementById('user_name').value
    if (user.length == 0) {
        alert('이름을 입력하세요')
    } else {
        fetch(`api/select?color=${color}&user=${user}`)
            .then(response => console.log(response))
            .catch(e => console.error(e))
    }
}
