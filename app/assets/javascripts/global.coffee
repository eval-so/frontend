$ ->
  $('.flash').delay(4000).fadeOut()
  $('#close_flash_early').click( -> closeFlashEarly())

closeFlashEarly = ->
  $('.flash').stop().fadeOut()