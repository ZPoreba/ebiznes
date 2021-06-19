import React, { Component } from 'react';
import './style.css';
import "antd/dist/antd.css";
import {Button, List, Spin} from "antd";
import { cartService } from './CartService';
import CartModal from "./components/CartModal";
import {withRouter} from "react-router-dom";
import {commonService} from "../Common/CommonService";


class CartView extends Component {

    constructor(props) {
        super(props);

        this.total = 0;
        this.state = {
            products: [],
            loading: true
        };
    }

    componentDidMount() {
        commonService.checkIfAuthenticated(this.props).then(result => {
            if(result) this.loadProducts();
        });
    }

    loadProducts = () => {
        cartService.getBucketProducts().then( async ids => {
            let products = [];

            let promises = ids.products.map( async productId => {
                let product = await cartService.getProductById(productId);

                if(products.some(p => p.id === product.product.id)) {
                    let filteredId= products.findIndex(p => p.id === product.product.id);
                    products[filteredId].counter = products[filteredId].counter + 1;
                }
                else {
                    product.product.counter = 1;
                    products.push(product.product);
                }

                return product;
            });

            Promise.all(promises).then(() => {
                this.setState({products: products, loading: false});
            });

        });
    }

    deleteFromCart = (productId) => {
        let newProducts = this.state.products;
        newProducts = newProducts.filter( product => {return product.id !== productId});

        cartService.deleteProductForUser(productId).then((resp) => {
            alert(resp);
            this.setState({products: newProducts});
        });
    }

    countTotal = () => {
        let total = 0;
        this.state.products.map(product => {
            total += product.counter * product.price;
            return product;
        });

        this.total = total;
        return total;
    }

    buyAll = async () => {
        let products = '';

        this.state.products.map( (item) => {
            products = products.concat(item.id.toString()).concat(',');
            return item;
        });

        this.refs.cartModal.show(this.total, products);
    }

    paymentCompleted = () => {
        this.setState({loading: true});
        cartService.deleteCart().then( resp => {
            this.setState({products: [], loading: false});
            alert('Order placed');
        })
    }

    render() {
        return(
            <div className="cartView">
                <Spin spinning={this.state.loading} className='spin' size="large">
                    <List
                        itemLayout="vertical"
                        size="large"
                        locale={{emptyText: 'No products in cart'}}
                        style={{marginLeft: '25%', marginRight: '25%'}}
                        pagination={{
                            size: "small",
                            pageSize: 6,
                        }}
                        dataSource={this.state.products}
                        renderItem={item => (
                            <List.Item
                                key={item.id}
                                style={{textAlign: "left"}}
                                extra={
                                    <div>
                                        <Button onClick={ e => this.deleteFromCart(item.id) }>Delete from cart</Button>
                                    </div>
                                }
                            >
                                <List.Item.Meta
                                    style={{textAlign: "left"}}
                                    title={<strong>{item.name}</strong>}
                                    description={<strong>Price: {item.price} zł, Amount: {item.counter}</strong>}
                                />
                                {item.description}
                            </List.Item>
                        )}
                    />
                    <div style={{marginTop: '30px'}}>
                        <Button type="primary"
                                size='large'
                                onClick={e => this.buyAll()}
                                style={{float: 'right', marginRight: '30%'}}>Buy</Button>
                        <div style={{textAlign: 'left', fontSize: 'x-large', marginLeft: '30%'}}>
                            <strong>Total: </strong>{this.countTotal()} zł
                        </div>
                    </div>
                    <CartModal ref='cartModal' paymentCompleted={this.paymentCompleted} />
                </Spin>
            </div>
        )
    }
}

export default withRouter(CartView);