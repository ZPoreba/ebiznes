# ebiznes

ZADANIE2 includes: <br />

- models with CRUD for: Cart, Product, Payment, User, Category, Order, Opinion, Return, WishList, DiscountCodes <br />
- models for many to many relations <br />
- controllers with CRUD for: Cart, Product, Payment, User, Category, Order, Opinion, Return, WishList, DiscountCodes <br />

## IMPORTANT

Create, update, delete actions require csrfToken parameter before you will test it ! 
To get token send GET to url: /[module_name]/token <br />
Where module_name is one of: product, cart, category, opinion, order, payment, user, return, wishlist, discountcode <br />
