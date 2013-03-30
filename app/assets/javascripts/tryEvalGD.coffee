$ ->
  $('textarea').keydown (event) =>
    if (event.keyCode == 10 or event.keyCode == 13) and event.ctrlKey
        $.ajax {
          "url": "/jsontest",
          "type": "POST",
          "data": JSON.stringify({
            "language": "ruby",
            "code": $('textarea').val()
          }),
          "dataType": "json",
          "contentType": "application/json; charset=utf-8",
          "success": (data) =>
            $('#result').text(JSON.stringify(data, null, 4))
        }