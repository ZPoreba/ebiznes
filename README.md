# ebiznes

ZADANIE1 includes: <br />

- play-slick configuration <br />
- play-slick-evolutions configuration <br />
- sqlite-jdbc configuration <br />
- CRUD for 10 modules <br />

## IMPORTANT

Create, update, delete actions require csrfToken parameter before you will test it ! 
To get token send GET to url: /<module>/token <br />
Where module is one of: product, cart, category, opinion, order, payment, user, return, wishlist, discountcode <br />

