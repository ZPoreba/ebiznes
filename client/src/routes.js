import React from 'react';
import {Route, Switch} from 'react-router-dom';
import ProductsView from './Products/ProductsView';
import CategoriesView from './Categories/CategoriesView';
import CartView from './Cart/CartView';
import WishListView from './WishList/WishListView';
import ProfileView from './Profile/ProfileView';
import OrdersView from './Orders/OrdersView';
import ReturnsView from './Returns/ReturnsView';


const routes = () => (
    <div>
        <Switch>
            <Route path="/search" component={CategoriesView} />
            <Route path="/products" component={ProductsView} />
            <Route path="/cart" component={CartView} />
            <Route path="/wishlist" component={WishListView} />
            <Route path="/profile" component={ProfileView} />
            <Route path="/orders" component={OrdersView} />
            <Route path="/returns" component={ReturnsView}/>
            <Route path="/" />
        </Switch>
    </div>
);

export default routes;