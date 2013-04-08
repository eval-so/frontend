import play.api.mvc.WithFilters

object Global extends WithFilters(new play.modules.statsd.api.StatsdFilter()) {
}
