import { combineReducers } from 'redux';
import stateReducer from '../modules/StateModule';
import userReducer from '../modules/UserModule';


const reduxModule = require('redux');

reduxModule.__DO_NOT_USE__ActionTypes.REPLACE = '@@redux/INIT';

export const makeRootReducer = (asyncReducers) => {
    const appReducer = combineReducers({
        user: userReducer,
        ...asyncReducers,
    });

    return (state, action) => appReducer(stateReducer(state, action), action);
};

export const injectReducer = (store, { key, reducer }) => {
    if (Object.hasOwnProperty.call(store.asyncReducers, key)) return;

    const s = store;
    s.asyncReducers[key] = reducer;
    s.replaceReducer(makeRootReducer(s.asyncReducers));
};

export default makeRootReducer;