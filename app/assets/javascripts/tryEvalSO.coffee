runEvaluation = ->
  $('#result').text("Loading...")
  $.ajax {
    "url": "/api/evaluate",
    "type": "POST",
    "data": JSON.stringify({
      "language": $('#eval-language').val(),
      "code": $('textarea').val()
    }),
    "dataType": "json",
    "contentType": "application/json; charset=utf-8",
    "success": (data) =>
      $('#result').text(JSON.stringify(data, null, 4))
    "error": (jqXHR, textStatus, errorThrown) =>
      $('#result').text("[" + textStatus + "] " + errorThrown)
  }

$ ->
  $(".chzn-select").chosen()
  $('textarea').keydown (event) =>
    if (event.keyCode == 10 or event.keyCode == 13) and event.ctrlKey
      runEvaluation()

  $('#eval-submit').click (event) =>
    runEvaluation()

  if (window.location.hash == '')
    $('#eval-language').val('ruby').trigger('liszt:updated')
  else
    $('#eval-language').val(window.location.hash.substring(1)).trigger('liszt:updated')
