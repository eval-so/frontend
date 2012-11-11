$ ->
  # Close flashes.
  $('.flash').delay(4000).fadeOut()
  $('#close_flash_early').click( -> closeFlashEarly())

  # Nothing to see here, move along.
  keys = []
  konami = '38,38,40,40,37,39,37,39,66,65'
  $(document).keydown (event) =>
    keys.push(event.keyCode)
    if (keys.toString().indexOf(konami) >= 0)
      alert('You have got to be kidding us.')
      alert('The konami code? Really?')
      alert("Ok. Fine. You've earned it.")
      document.location = 'http://www.youtube.com/watch?v=oHg5SJYRHA0'
      keys = []

# Called when a user clicks a flash's [x].
closeFlashEarly = ->
  $('.flash').stop().fadeOut()