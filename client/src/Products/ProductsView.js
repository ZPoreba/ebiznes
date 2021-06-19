import React, { Component } from 'react';
import './style.css';
import { productsService } from './ProductsService';
import { Spin } from "antd";
import { List, Button, Modal, Avatar } from 'antd';
import { MessageOutlined } from '@ant-design/icons';
import {commonService} from "../Common/CommonService";
import {withRouter} from "react-router-dom";


class ProductsView extends Component {

    constructor(props) {
        super(props);

        let splitPath = window.location.pathname.split("/");
        let categoryId = 1;
        if(splitPath.length === 3) categoryId = parseInt(splitPath[2]);

        this.state = {
            categoryId: categoryId,
            loading: true,
            products: [],
            visible: false,
            currentOpinions: []
        }
    }

    componentDidMount() {
        commonService.checkIfAuthenticated(this.props).then(result => {
            if(result) this.loadProducts();
        });
    }

    loadProducts = () => {
        if (this.state.categoryId === 0) {
            productsService.getAllProducts().then((resp) => {
                this.setState({products: resp, loading: false})
            });
        }
        else {
            productsService.getProducts(this.state.categoryId).then((resp) => {
                this.setState({products: resp, loading: false})
            });
        }
    }

    showOpinion = (productId) => {
        this.setState({loading: true}, async () => {
            let resp = await productsService.getOpinionsForProduct(productId);
            resp = await Promise.all(
                resp.map(async opinion => {
                    let user = (await productsService.getUserById(opinion.userId));
                    opinion.userId = user.fullName;
                    opinion.avatarURL = user.avatarURL;
                    return opinion;
                })
            );

            this.setState({currentOpinions: resp, loading: false, visible: true});
        });
    }

    createModal = () => {
        return (
            <Modal
                title="Opinions"
                visible={this.state.visible}
                onCancel={() => this.setState({visible: false})}
                footer={null}
            >
                    <List
                        itemLayout="horizontal"
                        dataSource={this.state.currentOpinions}
                        locale={{ emptyText: 'No opinions about this product' }}
                        pagination={{
                            size: "small",
                            pageSize: 4,
                        }}
                        renderItem={item => (
                            <List.Item>
                                <List.Item.Meta
                                    avatar={
                                        <Avatar src={item.avatarURL} />
                                    }
                                    title={<strong>{item.userId}</strong>}
                                    description={item.content}
                                />
                            </List.Item>
                        )}
                    />
            </Modal>
        )
    }

    addToBucket = (productId) => {
        productsService.addToBucket(productId).then((resp) => {
            alert(resp);
        });
    }

    addToWishList = (productId) => {
        productsService.addToWishList(productId).then((resp) => {
            alert(resp);
        });
    }

    render() {
        return(
            <div className="productsView">
                <Spin spinning={this.state.loading} className='spin' size="large">
                    <List
                        itemLayout="vertical"
                        size="large"
                        locale={{ emptyText: 'No products in this category' }}
                        style={{marginLeft: '25%', marginRight: '25%'}}
                        pagination={{
                            size: "small",
                            pageSize: 5,
                        }}
                        dataSource={this.state.products}
                        renderItem={item => (
                            <List.Item
                                key={item.id}
                                style={{textAlign: "left"}}
                                actions={[
                                    <div onClick={ e => this.showOpinion(item.id) } key="list-vertical-message" >
                                    <MessageOutlined style={{marginRight: '10px'}} />
                                     show opinions
                                    </div>
                                ]}
                                extra={
                                    <div>
                                        <Button onClick={ e => this.addToBucket(item.id) }>Add to cart</Button>
                                        <br />
                                        <br />
                                        <Button onClick={ e => this.addToWishList(item.id) }>Add to wish list</Button>
                                    </div>
                                }
                            >
                                <List.Item.Meta
                                    style={{textAlign: "left"}}
                                    title={<strong>{item.name}</strong>}
                                    description={<strong>Price: {item.price} zł</strong>}
                                />
                                {item.description}
                            </List.Item>
                        )}
                    />
                    {
                        this.state.visible ?
                        this.createModal() : null
                    }
                </Spin>
            </div>
        )
    }
}

export default withRouter(ProductsView);