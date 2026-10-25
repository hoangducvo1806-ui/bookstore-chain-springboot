(function($) {

    "use strict";

    var searchPopup = function() {
      // open search box
      $('#header-nav').on('click', '.search-button', function(e) {
        $('.search-popup').toggleClass('is-visible');
      });

      $('#header-nav').on('click', '.btn-close-search', function(e) {
        $('.search-popup').toggleClass('is-visible');
      });
      
      $(".search-popup-trigger").on("click", function(b) {
          b.preventDefault();
          $(".search-popup").addClass("is-visible");
          setTimeout(function() {
              $(".search-popup").find("#search-popup").focus();
          }, 350);
      });
      $(".search-popup").on("click", function(b) {
          if ($(b.target).is(".search-popup-close") || $(b.target).is(".search-popup-close svg") || $(b.target).is(".search-popup-close path") || $(b.target).is(".search-popup")) {
            b.preventDefault();
            $(this).removeClass("is-visible");
          }
      });
      $(document).keyup(function(b) {
          if (b.which === 27) {
            $(".search-popup").removeClass("is-visible");
          }
      });
    };

    var countdownTimer = function() {
      function getTimeRemaining(endtime) {
        const total = Date.parse(endtime) - Date.parse(new Date());
        const seconds = Math.floor((total / 1000) % 60);
        const minutes = Math.floor((total / 1000 / 60) % 60);
        const hours = Math.floor((total / (1000 * 60 * 60)) % 24);
        const days = Math.floor(total / (1000 * 60 * 60 * 24));
        return {
          total,
          days,
          hours,
          minutes,
          seconds
        };
      }
  
      function initializeClock(id, endtime) {
        const clock = document.getElementById(id);
        if (!clock) return;
        const daysSpan = clock.querySelector('.days');
        const hoursSpan = clock.querySelector('.hours');
        const minutesSpan = clock.querySelector('.minutes');
        const secondsSpan = clock.querySelector('.seconds');
  
        function updateClock() {
          const t = getTimeRemaining(endtime);
          if (daysSpan) daysSpan.innerHTML = t.days;
          if (hoursSpan) hoursSpan.innerHTML = ('0' + t.hours).slice(-2);
          if (minutesSpan) minutesSpan.innerHTML = ('0' + t.minutes).slice(-2);
          if (secondsSpan) secondsSpan.innerHTML = ('0' + t.seconds).slice(-2);
          if (t.total <= 0) {
            clearInterval(timeinterval);
          }
        }
        updateClock();
        const timeinterval = setInterval(updateClock, 1000);
      }
  
      $('#countdown-clock').each(function(){
        const deadline = new Date(Date.parse(new Date()) + 28 * 24 * 60 * 60 * 1000);
        initializeClock('countdown-clock', deadline);
      });
    };

    var initProductQty = function(){
      $('.product-qty').each(function(){
        var $el_product = $(this);
        $el_product.find('.quantity-right-plus').click(function(e){
            e.preventDefault();
            var quantity = parseInt($el_product.find('#quantity').val());
            $el_product.find('#quantity').val(quantity + 1);
        });

        $el_product.find('.quantity-left-minus').click(function(e){
            e.preventDefault();
            var quantity = parseInt($el_product.find('#quantity').val());
            if(quantity>0){
              $el_product.find('#quantity').val(quantity - 1);
            }
        });
      });
    };

    $(document).ready(function() {
      searchPopup();
      initProductQty();
      countdownTimer();

      /* Video */
      var $videoSrc;  
      $('.play-btn').click(function() {
        $videoSrc = $(this).data("src");
      });

      $('#myModal').on('shown.bs.modal', function (e) {
        $("#video").attr('src', $videoSrc + "?autoplay=1&amp;modestbranding=1&amp;showinfo=0"); 
      });

      $('#myModal').on('hide.bs.modal', function (e) {
        $("#video").attr('src', $videoSrc); 
      });

      if ($('.main-swiper').length) {
        new Swiper(".main-swiper", {
          speed: 500,
          navigation: {
            nextEl: ".main-slider-button-next",
            prevEl: ".main-slider-button-prev",
          },
        });
      }

      if ($('.product-swiper').length) {
        new Swiper(".product-swiper", {
          spaceBetween: 20,        
          navigation: {
            nextEl: ".product-slider-button-next",
            prevEl: ".product-slider-button-prev",
          },
          breakpoints: {
            0: { slidesPerView: 1 },
            660: { slidesPerView: 3 },
            980: { slidesPerView: 4 },
            1500: { slidesPerView: 5 }
          },
        });
      }

      if ($('.testimonial-swiper').length) {
        new Swiper(".testimonial-swiper", {
          slidesPerView: 1,
          spaceBetween: 20,
          navigation: {
            nextEl: ".testimonial-button-next",
            prevEl: ".testimonial-button-prev",
          },
        });
      }

      if ($('.thumb-swiper').length && $('.large-swiper').length) {
        var thumb_slider = new Swiper(".thumb-swiper", {
          slidesPerView: 1,
        });
        new Swiper(".large-swiper", {
          spaceBetween: 10,
          effect: 'fade',
          thumbs: {
            swiper: thumb_slider,
          },
        });
      }

    }); // End of a document ready

    window.addEventListener("load", function () {
      const preloader = document.getElementById("preloader");
      if (preloader) {
        preloader.classList.add("hide-preloader");
      }
    });

})(jQuery);
