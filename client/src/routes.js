import React from 'react';
import {Route, Switch} from 'react-router-dom';
import ProductsView from './Products/ProductsView';
import CategoriesView from './Categories/CategoriesView';
import CartView from './Cart/CartView';
import WishListView from './WishList/WishListView';
import ProfileView from './Profile/ProfileView';
import OrdersView from './Orders/OrdersView';
import ReturnsView from './Returns/ReturnsView';
import LoginView from "./Auth/LoginView";
import RegistrationView from "./Auth/RegistrationView";
import AuthProvider from "./Auth/AuthProvider";


const routes = () => (
    <div>
        <Switch>
            <Route path="/search" component={CategoriesView}/>
            <Route path="/products" component={ProductsView}/>
            <Route path="/cart" component={CartView}/>
            <Route path="/wishlist" component={WishListView}/>
            <Route path="/profile" component={ProfileView}/>
            <Route path="/orders" component={OrdersView}/>
            <Route path="/returns" component={ReturnsView}/>
            <Route path="/login" component={LoginView}/>
            <Route path="/register" component={RegistrationView}/>
            <Route path="/authenticated/google" render={(props) => (<AuthProvider {...props} provider='google'/>)}/>
            <Route path="/authenticated/facebook" render={(props) => (<AuthProvider {...props} provider='facebook'/>)}/>
            <Route path="/" />
        </Switch>
    </div>
);

export default routes;