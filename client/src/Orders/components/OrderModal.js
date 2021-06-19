import React, { Component } from 'react';
import "antd/dist/antd.css";
import {Button, List, Modal, Comment, Form, Input} from "antd";
import { ordersService } from '../OrdersService';


const { TextArea } = Input;

class OrderModal extends Component {

    constructor(props) {
        super(props);

        this.state = {
            visible: false,
            loading: true,
            products: [],
            disabled: {},
            submitting: false,
            value: ''
        };
    }

    Editor = (props) => {
        let filteredId = this.state.products.findIndex(p => p.id === props.id);
        let product = this.state.products[filteredId];
        product = product ? product: {};

        return (
            <div>
                <Form.Item>
                    <TextArea style={{height: '70px'}} rows={4} onChange={e => this.handleChange(e, filteredId)} value={product.value}/>
                </Form.Item>
                <Form.Item>
                    <Button htmlType="submit" loading={product.submitting} onClick={e => this.handleSubmit(filteredId)} type="primary">
                        Add opinion
                    </Button>
                </Form.Item>
            </div>);
    };

    handleSubmit = (id) => {
        if (!this.state.products[id].value) {
            return;
        }

        let products = this.state.products;
        products[id].submitting = true;

        this.setState({
            products: products,
        }, () => {

            products = this.state.products;

            let content = products[id].value;
            let productId = products[id].id;
            ordersService.createOpinion(content, productId).then(resp => {

                alert(resp);
                products[id].submitting = false;
                products[id].value = '';

                this.setState({
                    products: products,
                });

            });

        });
    };

    handleChange = (e, id) => {
        let products = this.state.products;
        products[id].value = e.target.value;

        this.setState({
            products: products,
        });
    };

    handleCancel = e => {
        this.setState({
            visible: false,
        });
    };

    loadProducts = (products) => {
        let productsArray = [];

        let promises = products.map( async productId => {
            let product = await ordersService.getProductById(productId);

            if(productsArray.some(p => p.id === product.product.id)) {
                let filteredId= productsArray.findIndex(p => p.id === product.product.id);
                productsArray[filteredId].counter = productsArray[filteredId].counter + 1;
            }
            else {
                product.product.counter = 1;
                product.product.submitting = false;
                product.product.value = '';
                productsArray.push(product.product);
            }

            return product;
        });

        Promise.all(promises).then(() => {
            this.setState({products: productsArray});
        });
    }

    show = (products) => {
        this.loadProducts(products);
        this.setState({visible: true, loading: false});
    }

    countTotal = () => {
        let total = 0;
        this.state.products.map(product => {
            total += product.price;
            return product;
        });

        return total;
    }

    returnProduct = (productId) => {
        ordersService.createReturn(productId).then( resp => {
            let disabled = this.state.disabled;
            disabled[productId] = true;
            alert(resp);
            this.setState({disabled: disabled})
        })
    }

    renderCommentInput = (id) => {
        return(
            <div>
                <Comment
                    content={
                        <this.Editor id={id} />
                    }
                />
            </div>
        );
    }

    render() {
        return(
            <Modal
                title="Order details"
                visible={this.state.visible}
                footer={false}
                onCancel={this.handleCancel}
                width={'70%'}
            >
                <List
                    itemLayout="vertical"
                    size="large"
                    locale={{emptyText: 'No products in order'}}
                    pagination={{
                        size: "small",
                        pageSize: 1,
                    }}
                    dataSource={this.state.products}
                    renderItem={item => (
                        <List.Item
                            key={item.id}
                            style={{textAlign: "left"}}
                            extra={
                                <div>
                                    <Button disabled={this.state.disabled[item.id]}
                                            onClick={ e => this.returnProduct(item.id) }>Return product</Button>
                                </div>
                            }
                        >
                            <List.Item.Meta
                                style={{textAlign: "left"}}
                                title={<strong>{item.name}</strong>}
                                description={<strong>Price: {item.price} zł, Amount: {item.counter}</strong>}
                            />
                            {item.description}
                            {this.renderCommentInput(item.id)}
                        </List.Item>
                    )}
                />
                <div style={{textAlign: 'left', marginTop: '10px', fontSize: 'x-large'}}>
                    <strong>Total: </strong>{this.countTotal()} zł
                </div>
            </Modal>
        );
    }

}

export default OrderModal;